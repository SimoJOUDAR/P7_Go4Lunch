package fr.joudar.go4lunch.ui.fragments.settings;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
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
import fr.joudar.go4lunch.ui.core.dialogs.TimeDialogPreference;
import fr.joudar.go4lunch.ui.core.dialogs.TimePreferenceDialog;

public class SettingsFragment extends PreferenceFragmentCompat {

    //public SettingsFragment() {} // TODO: Delete?

    SeekBarPreference searchRadius;
    SwitchPreferenceCompat notificationEnabled;
    TimeDialogPreference lunchReminder;
    Preference workplaceField;
    Preference deleteButton;

    private final String JOB_TAG = "NOTIFICATION_DATA_FETCHING_JOB";


    Preference.OnPreferenceChangeListener searchRadiusListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(@NonNull Preference preference, Object newValue) {
            int value = ((SeekBarPreference) preference).getValue();
            String radius = String.valueOf(value*1000);
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
            sharedPreferences.edit().putString("search_radius", radius).apply();
            return true;
        }
    };

    //Lunch notifications
    Preference.OnPreferenceChangeListener notificationSwitchListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(@NonNull Preference preference, Object newValue) {
            final boolean enabled = (boolean) newValue;

            //lunchReminder.setEnabled(enabled); // TODO: Check first if xml "dependency" attribute isn't enough
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

    Preference.OnPreferenceClickListener lunchReminderListener = new Preference.OnPreferenceClickListener() {
        @Override
        public boolean onPreferenceClick(@NonNull Preference preference) {
            TimePreferenceDialog.getInstance(preference.getKey())
                    .show(getParentFragmentManager(), "Time preference dialog");
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
//        setPreferencesFromResource(R.xml.preferences, rootKey);
//        viewsBinding();
//        searchRadius.setOnPreferenceChangeListener(searchRadiusListener);
//        notificationEnabled.setOnPreferenceChangeListener(notificationSwitchListener);
//        lunchReminder.setOnPreferenceClickListener(lunchReminderListener);
//        workplaceField.setSummary(workplaceField.getSharedPreferences().getString("workplace", "N/A"));
//        workplaceField.setOnPreferenceClickListener(workplaceClickListener);
//        deleteButton.setOnPreferenceClickListener(deleteBtnClickListener);
    }

    private void viewsBinding(){
        searchRadius = findPreference("search_radius");
        notificationEnabled = findPreference("notification_enabled");
        lunchReminder = findPreference("lunch_reminder");
        workplaceField = findPreference("workplace");
        deleteButton = findPreference("delete");
    }

    @Override
    public void onResume() {
        super.onResume();
        ((HomepageActivity)getActivity()).settingsFragmentDisplayOptions();
    }


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

    /***********************************************************************************************
     ** Notification Work
     **********************************************************************************************/

    private void scheduleNotificationJob(Context context, @Nullable Calendar dueDate) {
        ((HomepageActivity)getActivity()).scheduleNotificationJob(context, dueDate);
    }

    private void deleteNotificationJob(Context context) {
        WorkManager.getInstance(context).cancelAllWorkByTag(JOB_TAG);
    }

}