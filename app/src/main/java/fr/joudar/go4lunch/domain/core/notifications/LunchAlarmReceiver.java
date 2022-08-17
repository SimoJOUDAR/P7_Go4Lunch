package fr.joudar.go4lunch.domain.core.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;


// Enqueues the pre-notification job
public class LunchAlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("LunchAlarmReceiver", "onReceive");
        LunchNotificationJobHandler.enqueueWork(context, intent);

    }
}
