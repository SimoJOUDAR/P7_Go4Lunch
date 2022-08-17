package fr.joudar.go4lunch.domain.core.notifications;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;

// Programs the Alarm to trigger the pre-Notification job
public class LunchAlarmHandler {

    public static final int ALARM_MANAGER_PENDING_INTENT_CODE = 1;
    private final Context context;
    private final AlarmManager alarmManager;

    // Constructor
    public LunchAlarmHandler(Context context) {
        this.context = context;
        this.alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    }

    //
    public void scheduleLunchAlarm(Calendar calendar, Class<?> componentClass) {
        if (calendar.before(Calendar.getInstance())) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                ALARM_MANAGER_PENDING_INTENT_CODE,
                new Intent(context, componentClass),
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY,
                pendingIntent
        );
    }

    public void cancelLunchAlarm(Class<?> componentClass) {

        final PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                ALARM_MANAGER_PENDING_INTENT_CODE,
                new Intent(context, componentClass),
                PendingIntent.FLAG_NO_CREATE
        );

        if (pendingIntent != null) alarmManager.cancel(pendingIntent);
    }



}
