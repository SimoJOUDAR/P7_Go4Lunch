package fr.joudar.go4lunch.domain.core.notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.navigation.NavDeepLinkBuilder;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import fr.joudar.go4lunch.R;
import fr.joudar.go4lunch.ui.activities.HomepageActivity;

public class NotificationBuilding extends Worker {

    private static final int NOTIFICATION_ID = 11;
    String NOTIFICATION_CHANNEL_ID = "Lunch_Time_Notification_Channel_Id";

    String username;
    String chosenRestaurantId = null;
    String chosenRestaurantName;
    String chosenRestaurantAddress;
    String joiningColleagues;

    public NotificationBuilding(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        launchNotification(context);
        return Result.success();
    }

    // Builds and displays the notification
    private void launchNotification(Context context) {
        final NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChanel(context, notificationManager);

        initData();

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
                .setContentText(getContentText(context))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(getContentText(context)))
                .setSmallIcon(R.drawable.ic_utensils)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .build();

        notificationManager.notify(NOTIFICATION_ID, notification);
    }


    // Creates a Notification Channel for Android version 26 or higher
    private void createNotificationChanel(Context context, NotificationManager notificationManager) {
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

    // Extracts data from the inputBundle
    private void initData() {
        username = getInputData().getString("username");
        chosenRestaurantId = getInputData().getString("chosenRestaurantId");
        chosenRestaurantName = getInputData().getString("chosenRestaurantName");
        chosenRestaurantAddress = getInputData().getString("chosenRestaurantAddress");
        joiningColleagues = getInputData().getString("joiningColleagues");
    }

    // Return the appropriate text to display on the Notification
    private String getContentText(Context context) {
        String contentText = null;
        if (chosenRestaurantId == null || chosenRestaurantId.isEmpty()) {
            contentText = context.getString(R.string.lunch_time_notification_msg_restaurantless, username);
        }
        else if (joiningColleagues == null || joiningColleagues.isEmpty()) {
            contentText = context.getString(R.string.lunch_time_notification_msg_unaccompanied, username, chosenRestaurantName, chosenRestaurantAddress);
        }

        else {
            contentText = context.getString(R.string.lunch_time_notification_msg, username, chosenRestaurantName, chosenRestaurantAddress, joiningColleagues);
        }

        return contentText;
    }

}
