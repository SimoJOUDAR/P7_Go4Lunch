package fr.joudar.go4lunch.domain.core.notifications;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;
import androidx.core.app.NotificationCompat;
import androidx.navigation.NavDeepLinkBuilder;


import java.util.Arrays;
import java.util.Calendar;

import javax.annotation.Nullable;
import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import fr.joudar.go4lunch.R;
import fr.joudar.go4lunch.domain.models.Place;
import fr.joudar.go4lunch.domain.models.User;
import fr.joudar.go4lunch.domain.utils.Callback;
import fr.joudar.go4lunch.repositories.FirebaseServicesRepository;
import fr.joudar.go4lunch.repositories.PlaceDetailsRepository;
import fr.joudar.go4lunch.ui.activities.HomepageActivity;

// Pre-Notification job - Fetching: currentUser, JoiningColleagues, ChosenRestaurantDetail
@AndroidEntryPoint
public class LunchNotificationJobHandler extends JobIntentService {

    private static final int NOTIFICATION_ID = 11;
    private static final int WORK_ID = 22;
    String NOTIFICATION_CHANNEL_ID = "Lunch_Time_Notification_Channel_Id";

    @Inject FirebaseServicesRepository firebaseServicesRepository;
    @Inject PlaceDetailsRepository placeDetailsRepository;

    Boolean data1Fetched = false;
    Boolean data2Fetched = false;
    Context context;
    User currentUser;

    String username;
    String chosenRestaurantId = null;
    String chosenRestaurantName;
    String chosenRestaurantAddress;
    String joiningColleagues;



    public static void enqueueWork(@NonNull Context context, @NonNull Intent intent){
        enqueueWork(context, LunchNotificationJobHandler.class, WORK_ID, intent);
    }

    public LunchNotificationJobHandler() {} // required by the system when declaring the service in the manifest

    public LunchNotificationJobHandler(Context context) {
        this.context = context;
    }


    @Override
    protected void onHandleWork(@NonNull Intent intent) {

        currentUser = firebaseServicesRepository.getCurrentUser();
        username = currentUser.getUsername();
        chosenRestaurantId = currentUser.getChosenRestaurantId();

        if (!chosenRestaurantId.isEmpty()) {
            if (!currentUser.getWorkplaceId().isEmpty()) {
                firebaseServicesRepository.getColleaguesByRestaurant(chosenRestaurantId, currentUser.getWorkplaceId(), colleaguesByRestaurantCallback);
            }
            placeDetailsRepository.getPlaceDetails(chosenRestaurantId, placeDetailCallback);
        }
    }

    Callback<User[]> colleaguesByRestaurantCallback = new Callback<User[]>() {
        @Override
        public void onSuccess(User[] colleagues) {
            Log.d("LunchNotificationWorker", "Colleagues fetched : " + Arrays.toString(colleagues));
            UsersToStringConverter(colleagues);
            data1Fetched = true;
            dataFetchingCompleteProguard();
        }

        @Override
        public void onFailure() {
            UsersToStringConverter(null);
            data1Fetched = true;
            dataFetchingCompleteProguard();
        }
    };

    Callback<Place> placeDetailCallback = new Callback<Place>() {
        @Override
        public void onSuccess(Place place) {
            chosenRestaurantName = place.getName();
            chosenRestaurantAddress = place.getVicinity();
            data2Fetched = true;
            dataFetchingCompleteProguard();
        }

        @Override
        public void onFailure() {
            chosenRestaurantName = null;
            chosenRestaurantAddress = null;
            data2Fetched = true;
            dataFetchingCompleteProguard();
        }
    };

    private void UsersToStringConverter(User[] users) {

        if (users != null) {
            StringBuilder builder = new StringBuilder();
            for (User user : users) {
                builder.append(user.getUsername()).append(", ");
                final int length = builder.length();
                builder.delete(length - 2, length);  // To remove the last comma-space
            }
            joiningColleagues = builder.toString();
        }
        else joiningColleagues = null;
    }

    private void dataFetchingCompleteProguard() {
        if (data1Fetched && data2Fetched)
        launchNotification();
    }

    private void createNotificationChanel(NotificationManager notificationManager) {
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

    private void launchNotification() {
        final NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
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
                .setContentText(getContentText())
                .setStyle(new NotificationCompat.BigTextStyle().bigText(getContentText()))
                .setSmallIcon(R.drawable.ic_utensils)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .build();

        notificationManager.notify(NOTIFICATION_ID, notification);
    }

    private String getContentText() {
        String contentText = null;
        if (chosenRestaurantId == null) {
            contentText = context.getString(R.string.lunch_time_notification_msg_restaurantless, username);
        }
        else if (joiningColleagues == null) {
            contentText = context.getString(R.string.lunch_time_notification_msg_unaccompanied, username, chosenRestaurantName, chosenRestaurantAddress);
        }

        else {
            contentText = context.getString(R.string.lunch_time_notification_msg, username, chosenRestaurantName, chosenRestaurantAddress, joiningColleagues);
        }

        return contentText;
    }


    // For SettingsFragment
    public static void setEnabled(Context context, boolean enabled, @Nullable Calendar calendar) {
        final LunchAlarmHandler lunchAlarmHandler = new LunchAlarmHandler(context);
        if (enabled) {
            if (calendar == null) throw new IllegalArgumentException();
            OnBootReceiver.enableNotifications(context);
            lunchAlarmHandler.scheduleLunchAlarm(calendar, LunchAlarmReceiver.class);  // Re-program the Notification
        } else {
            OnBootReceiver.disableNotifications(context);
            lunchAlarmHandler.cancelLunchAlarm(LunchAlarmReceiver.class);
        }
    }
}
