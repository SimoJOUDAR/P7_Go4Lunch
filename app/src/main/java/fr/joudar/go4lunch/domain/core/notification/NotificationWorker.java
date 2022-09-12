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
import androidx.core.app.NotificationManagerCompat;
import androidx.hilt.work.HiltWorker;
import androidx.navigation.NavDeepLinkBuilder;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedInject;
import fr.joudar.go4lunch.R;
import fr.joudar.go4lunch.domain.models.Place;
import fr.joudar.go4lunch.domain.models.User;
import fr.joudar.go4lunch.domain.services.FirebaseServicesProvider;
import fr.joudar.go4lunch.domain.utils.Callback;
import fr.joudar.go4lunch.repositories.FirebaseServicesRepository;
import fr.joudar.go4lunch.repositories.PlaceDetailsRepository;
import fr.joudar.go4lunch.ui.activities.HomepageActivity;

@HiltWorker
public class NotificationWorker extends Worker {

    private final String TAG = "NotificationWorker";
    private static final int NOTIFICATION_ID = 11;
    private String NOTIFICATION_CHANNEL_ID = "Lunch_Time_Notification_Channel_Id";

    private final FirebaseServicesProvider firebaseServicesProvider;

    User currentUser;
    String username = null;
    String workplaceId = null;
    String chosenRestaurantId = null;
    String chosenRestaurantName = null;
    String chosenRestaurantAddress = null;
    String joiningColleagues = null;
    String contentText = null;

    Callback<User[]> colleaguesByRestaurantCallback = new Callback<User[]>() {
        @Override
        public void onSuccess(User[] colleagues) {
            Log.d(TAG, "colleaguesByRestaurantCallback - onSuccess : Colleagues fetched : " + Arrays.toString(colleagues));
            UsersToStringConverter(colleagues);
        }

        @Override
        public void onFailure() {
            Log.d(TAG, "colleaguesByRestaurantCallback - onFailure");
            getContentText(getApplicationContext());
        }
    };

    @AssistedInject
    public NotificationWorker(
            @Assisted @NonNull Context context,
            @Assisted @NonNull WorkerParameters workerParams,
            FirebaseServicesProvider firebaseServicesProvider) {
        super(context, workerParams);
        Log.d(TAG, "Constructor");
        this.firebaseServicesProvider = firebaseServicesProvider;
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "doWork");
        initData();
        return Result.success();
    }

    private void initData() {
        Log.d(TAG, "initData");
        currentUser = firebaseServicesProvider.getCurrentUser();
        username = currentUser.getUsername();
        workplaceId = currentUser.getWorkplaceId();
        chosenRestaurantId = currentUser.getChosenRestaurantId();
        chosenRestaurantName = currentUser.getChosenRestaurantName();
        chosenRestaurantAddress = currentUser.getWorkplaceAddress();

        if (chosenRestaurantId != null && !chosenRestaurantId.isEmpty()) {
            if (workplaceId != null && !workplaceId.isEmpty()) {
                firebaseServicesProvider.getColleaguesByRestaurant(chosenRestaurantId, colleaguesByRestaurantCallback);
            }
            else
                getContentText(getApplicationContext());
        }
        else
            getContentText(getApplicationContext());
    }

    private void UsersToStringConverter(User[] users) {
        Log.d(TAG, "UsersToStringConverter");
        if (users != null) {
            StringBuilder builder = new StringBuilder();
            for (User user : users) {
                builder.append(user.getUsername()).append(", ");
            }
            final int length = builder.length();
            builder.delete(length - 2, length);  // To remove the last comma-space
            joiningColleagues = builder.toString();
        }
        else
            joiningColleagues = null;
        getContentText(getApplicationContext());
    }

    // Return the appropriate text to display on the Notification
    private void getContentText(Context context) {
        Log.d(TAG, "getContentText");
        if (chosenRestaurantId == null || chosenRestaurantId.isEmpty()) {
            contentText = context.getString(R.string.lunch_time_notification_msg_restaurantless, username);
        }
        else if (joiningColleagues == null || joiningColleagues.isEmpty()) {
            contentText = context.getString(R.string.lunch_time_notification_msg_unaccompanied, username, chosenRestaurantName, chosenRestaurantAddress);
        }

        else {
            contentText = context.getString(R.string.lunch_time_notification_msg, username, chosenRestaurantName, chosenRestaurantAddress, joiningColleagues);
        }

        launchNotification(getApplicationContext());
    }

    // Builds and displays the notification
    private void launchNotification(Context context) {
        Log.d(TAG, "launchNotification");
        final NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChanel(context, notificationManager);

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
    }


    // Creates a Notification Channel for Android version 26 or higher
    private void createNotificationChanel(Context context, NotificationManager notificationManager) {
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

}
