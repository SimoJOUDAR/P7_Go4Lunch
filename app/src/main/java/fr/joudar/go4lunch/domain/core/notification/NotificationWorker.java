package fr.joudar.go4lunch.domain.core.notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.hilt.work.HiltWorker;
import androidx.navigation.NavDeepLinkBuilder;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.concurrent.TimeUnit;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedInject;
import fr.joudar.go4lunch.R;
import fr.joudar.go4lunch.ui.activities.HomepageActivity;

public class NotificationWorker extends Worker {

    private final String TAG = "NotificationWorker";
    private static final int NOTIFICATION_ID = 11;
    private final String NOTIFICATION_CHANNEL_ID = "Lunch_Time_Notification_Channel_Id";

    private final Context context;
    private final FirebaseFirestore firestore;
    private final FirebaseAuth firebaseAuth;

    String USERNAME = "username";
    String WORKPLACE_ID = "workplaceId";
    String CHOSEN_RESTAURANT_ID = "chosenRestaurantId";
    String CHOSEN_RESTAURANT_NAME = "chosenRestaurantName";
    String CHOSEN_RESTAURANT_ADDRESS = "chosenRestaurantAddress";

    String userId = null;
    String username = null;
    String workplaceId = null;
    String chosenRestaurantId = null;
    String chosenRestaurantName = null;
    String chosenRestaurantAddress = null;
    String joiningColleagues = null;
    String contentText = null;

    public NotificationWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        Log.d(TAG, "Constructor");
        this.context = context;
        firestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "doWork");
        final FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser != null) {
            userId = firebaseUser.getUid();
            initUserDataFromFirestore();
        } else
            getContentText();


        return Result.success();
    }

    /***********************************************************************************************
     ** Data fetching
     **********************************************************************************************/

    private void initUserDataFromFirestore() {
        Log.d(TAG, "initUserDataFromFirestore");
        firestore.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot userDocument) {
                        initData(userDocument);
                    }
                });
    }

    private void initData(DocumentSnapshot userDocument) {
        Log.d(TAG, "initData");
        username = userDocument.getString(USERNAME);
        workplaceId = userDocument.getString(WORKPLACE_ID);
        chosenRestaurantId = userDocument.getString(CHOSEN_RESTAURANT_ID);
        chosenRestaurantName = userDocument.getString(CHOSEN_RESTAURANT_NAME);
        chosenRestaurantAddress = userDocument.getString(CHOSEN_RESTAURANT_ADDRESS);

        if (chosenRestaurantId != null && !chosenRestaurantId.isEmpty()) {
            if (workplaceId != null && !workplaceId.isEmpty()) {
                getColleaguesByRestaurant();
            }
            else
                getContentText();
        }
        else
            getContentText();
    }

    public void getColleaguesByRestaurant() {
        Log.d(TAG, "getColleaguesByRestaurant");
        firestore.collection("users")
                .whereEqualTo(WORKPLACE_ID, workplaceId)
                .whereEqualTo(CHOSEN_RESTAURANT_ID, chosenRestaurantId)
                .get()
                .addOnSuccessListener(
                        snapshots -> joiningColleaguesBuilder(snapshots.getDocuments()))
                .addOnFailureListener(__ -> getContentText());
    }

    private void joiningColleaguesBuilder(List<DocumentSnapshot> usersDocuments) {
        Log.d(TAG, "snapshotsToArrayConverter");
        StringBuilder builder = new StringBuilder();
        for (DocumentSnapshot userDoc : usersDocuments) {
            if (!userDoc.getId().equals(userId)) {
                builder.append(userDoc.getString(USERNAME)).append(", ");
            }
        }
        final int length = builder.length();
        builder.delete(length - 2, length);  // To remove the last comma-space
        joiningColleagues = builder.toString();

        getContentText();
    }

    // Return the appropriate text to display on the Notification
    private void getContentText() {
        Log.d(TAG, "getContentText");

        if (userId == null || userId.isEmpty())
            contentText = context.getString(R.string.lunch_time_notification_msg_userless);
        else if (chosenRestaurantId == null || chosenRestaurantId.isEmpty())
            contentText = context.getString(R.string.lunch_time_notification_msg_restaurantless, username);
        else if (joiningColleagues == null || joiningColleagues.isEmpty())
            contentText = context.getString(R.string.lunch_time_notification_msg_unaccompanied, username, chosenRestaurantName, chosenRestaurantAddress);

        else
            contentText = context.getString(R.string.lunch_time_notification_msg, username, chosenRestaurantName, chosenRestaurantAddress, joiningColleagues);

        launchNotification();
    }

    /***********************************************************************************************
     ** Notification
     **********************************************************************************************/

    // Builds and displays the notification
    private void launchNotification() {
        Log.d(TAG, "launchNotification");
        final NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChanel(notificationManager);

        Bundle bundle = new Bundle();
        bundle.putString("placeId", chosenRestaurantId);

        PendingIntent pendingIntent = new NavDeepLinkBuilder(context)
                .setGraph(R.navigation.nav_graph)
                .setDestination(R.id.restaurantDetailsFragment)
                .setArguments(bundle)
                .setComponentName(HomepageActivity.class)
                .createPendingIntent();

        Notification notification = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                .setContentTitle(context.getString(R.string.lunch_notification_title))
                .setContentText(contentText)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(contentText))
                .setSmallIcon(R.drawable.ic_utensils)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build();

        notificationManager.notify(NOTIFICATION_ID, notification);
        chainNextWork();
    }


    // Creates a Notification Channel for Android version 26 or higher
    private void createNotificationChanel(NotificationManager notificationManager) {
        Log.d(TAG, "createNotificationChanel");
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            final String notificationChannelName = context.getString(R.string.notification_channel_name);
            final String notificationChannelDescription =
                    context.getString(R.string.notification_channel_description);
            final NotificationChannel notificationChannel =
                    new NotificationChannel(
                            NOTIFICATION_CHANNEL_ID, notificationChannelName, NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.setDescription(notificationChannelDescription);
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }


    /***********************************************************************************************
     ** Reset chosenRestaurant
     **********************************************************************************************/
    // Chains another worker responsible to reset chosenRestaurant data in Firestore
    private void chainNextWork() {

        final OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(ResetWorker.class)
                .setInitialDelay(2, TimeUnit.HOURS)
                .addTag("ResetWorker")
                .build();

        WorkManager.getInstance(context).enqueue(workRequest);
    }

}
