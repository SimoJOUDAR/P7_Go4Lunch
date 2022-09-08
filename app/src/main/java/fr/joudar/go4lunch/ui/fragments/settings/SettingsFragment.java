package fr.joudar.go4lunch.ui.fragments.settings;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SeekBarPreference;
import androidx.preference.SwitchPreferenceCompat;
import androidx.work.WorkManager;

import java.util.Calendar;
import fr.joudar.go4lunch.R;
import fr.joudar.go4lunch.domain.utils.Callback;
import fr.joudar.go4lunch.ui.activities.HomepageActivity;
import fr.joudar.go4lunch.ui.core.dialogs.TimePreference;
import fr.joudar.go4lunch.ui.core.dialogs.TimePreferenceDialog;

public class SettingsFragment extends PreferenceFragmentCompat {

    private String TAG = "SettingsFragment";

    public SettingsFragment() {} // TODO: Delete?

    SwitchPreferenceCompat notificationEnabled;
    TimePreference lunchReminder;
    Preference workplaceField;
    Preference deleteButton;

    private final String JOB_TAG = "NOTIFICATION_DATA_FETCHING_JOB";

    //Lunch notifications
    Preference.OnPreferenceChangeListener notificationSwitchListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(@NonNull Preference preference, Object newValue) {
            final boolean enabled = (boolean) newValue;

            //LunchNotificationJobHandler.setEnabled(getContext(), enabled, lunchReminder.getPersistedTime());  //Uses the deprecated JobIntentService

            // Uses WorkerManager
            if (enabled){
                Calendar calendar = lunchReminder.getPersistedTime();
                if (calendar == null)
                    throw new IllegalArgumentException();
                scheduleNotificationJob(getContext(), calendar);
            }
            else
                deleteNotificationJob(getContext());
            return true;
        }
    };

    Preference.OnPreferenceClickListener workplaceClickListener = new Preference.OnPreferenceClickListener() {
        @Override
        public boolean onPreferenceClick(@NonNull Preference preference) {

            // Updates "workplace" PreferenceSummary
            Callback<String> summaryCallback = new Callback<String>() {
                @Override
                public void onSuccess(String workplace) {
                    preference.setSummary(workplace);
                }

                @Override
                public void onFailure() {

                }
            };

            ((HomepageActivity) getActivity()).launchWorkplacePickerDialog(summaryCallback);

            return true;
        }
    };

    Preference.OnPreferenceClickListener deleteBtnClickListener = new Preference.OnPreferenceClickListener() {
        @Override
        public boolean onPreferenceClick(@NonNull Preference preference) {

            new AlertDialog.Builder(getContext())
                    .setTitle("Delete account confirmation")
                    .setMessage("You are about to permanently delete this account. This action is irreversible!\\nAre you sure?")
                    .setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.cancel())
                    .setPositiveButton("Delete", (dialogInterface, i) -> ((HomepageActivity) getActivity()).deleteCurrentUserAccount())
                    .create()
                    .show();
            return true;
        }
    };

    @Override
    public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
        Log.d(TAG, "onCreatePreferences _START_");

        Log.d(TAG, "onCreatePreferences _setPreferencesFromResource_");
        setPreferencesFromResource(R.xml.preferences, rootKey); // TODO: Bug ? Why?

        viewsBinding();

        Log.d(TAG, "notificationEnabled.setOnPreferenceChangeListener _START_");
        notificationEnabled.setOnPreferenceChangeListener(notificationSwitchListener);
        Log.d(TAG, "notificationEnabled.setOnPreferenceChangeListener _FINISH_");

        Log.d(TAG, "workplaceField.setSummary _START_");
        workplaceField.setSummary(workplaceField.getSharedPreferences().getString("workplace", "N/A"));
        Log.d(TAG, "workplaceField.setSummary _FINISH_");

        Log.d(TAG, "workplaceField.setOnPreferenceClickListener _START_");
        workplaceField.setOnPreferenceClickListener(workplaceClickListener);
        Log.d(TAG, "workplaceField.setOnPreferenceClickListener _FINISH_");

        Log.d(TAG, "deleteButton.setOnPreferenceClickListener _START_");
        deleteButton.setOnPreferenceClickListener(deleteBtnClickListener);
        Log.d(TAG, "deleteButton.setOnPreferenceClickListener _FINISH_");

        Log.d(TAG, "onCreatePreferences _FINISH_");
    }

    private void viewsBinding(){
        Log.d(TAG, "viewsBinding _START_");
        notificationEnabled = findPreference("notification_enabled");
        lunchReminder = findPreference("lunch_reminder");  //TODO: Problem here - TimeDialogPreference
        workplaceField = findPreference("workplace");
        deleteButton = findPreference("delete");
        Log.d(TAG, "viewsBinding _FINISH_");
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume _START_");
        super.onResume();
        ((HomepageActivity)getActivity()).settingsFragmentDisplayOptions();
        Log.d(TAG, "onResume _FINISH_");
    }


    @Override
    public void onDisplayPreferenceDialog(@NonNull Preference preference) {
        if (preference instanceof TimePreference) {
            final TimePreferenceDialog dialog = TimePreferenceDialog.getInstance(preference.getKey());
            dialog.setTargetFragment(this, 1);
            dialog.show(getParentFragmentManager(), "Time preference dialog");
        }
        else
            super.onDisplayPreferenceDialog(preference);
    }

    /***********************************************************************************************
     ** Notification Work
     **********************************************************************************************/

    // To enable or disable notifications //Uses the deprecated JobIntentService
//    private void enable(Context context, boolean enabled, @Nullable Calendar calendar) {
//        final LunchAlarmHandler lunchAlarmHandler = new LunchAlarmHandler(context);
//        if (enabled) {
//            if (calendar == null) throw new IllegalArgumentException();
//            OnBootReceiver.enableNotifications(context);
//            lunchAlarmHandler.scheduleLunchAlarm(calendar, LunchAlarmReceiver.class);  // Re-program the Notification
//        } else {
//            OnBootReceiver.disableNotifications(context);
//            lunchAlarmHandler.cancelLunchAlarm(LunchAlarmReceiver.class);
//        }
//    }

    private void scheduleNotificationJob(Context context, @Nullable Calendar dueDate) {
        Log.d(TAG, "scheduleNotificationJob _START_");
        ((HomepageActivity)getActivity()).scheduleNotificationJob(context, dueDate);
        Log.d(TAG, "scheduleNotificationJob _FINISH_");
    }

    private void deleteNotificationJob(Context context) {
        Log.d(TAG, "deleteNotificationJob _START_");
        WorkManager.getInstance(context).cancelAllWorkByTag(JOB_TAG);
        Log.d(TAG, "deleteNotificationJob _FINISH_");
    }

}