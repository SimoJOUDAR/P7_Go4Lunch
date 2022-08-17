package fr.joudar.go4lunch.domain.core.notifications;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import java.util.Calendar;

import fr.joudar.go4lunch.ui.core.dialogs.TimeDialogPreference;

// Triggered after each boot to schedule notifications
public class OnBootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        final Calendar calendar = new TimeDialogPreference(context).getPersistedTime();
        new LunchAlarmHandler(context).scheduleLunchAlarm(calendar, LunchAlarmReceiver.class);
    }

    public static void enableNotifications(Context context) {
        final ComponentName receiver = new ComponentName(context, OnBootReceiver.class);
        context.getPackageManager().setComponentEnabledSetting(receiver, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
    }

    public static void disableNotifications(Context context) {
        final ComponentName receiver = new ComponentName(context, OnBootReceiver.class);
        context.getPackageManager().setComponentEnabledSetting(receiver, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
    }
}
