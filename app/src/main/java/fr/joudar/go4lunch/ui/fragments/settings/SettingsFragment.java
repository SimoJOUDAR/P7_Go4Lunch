package fr.joudar.go4lunch.ui.fragments.settings;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavBackStackEntry;
import androidx.navigation.NavController;
import androidx.navigation.NavDeepLinkBuilder;
import androidx.navigation.Navigation;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SeekBarPreference;
import androidx.preference.SwitchPreferenceCompat;
import androidx.work.WorkManager;

import com.google.android.gms.maps.SupportMapFragment;

import java.util.Arrays;
import java.util.Calendar;

import dagger.hilt.android.internal.lifecycle.HiltViewModelFactory;
import fr.joudar.go4lunch.R;
import fr.joudar.go4lunch.domain.models.User;
import fr.joudar.go4lunch.domain.utils.Callback;
import fr.joudar.go4lunch.ui.activities.HomepageActivity;
import fr.joudar.go4lunch.ui.core.dialogs.TimePreference;
import fr.joudar.go4lunch.ui.core.dialogs.TimePreferenceDialog;
import fr.joudar.go4lunch.viewmodel.HomepageViewModel;

public class SettingsFragment extends PreferenceFragmentCompat {

    private final String TAG = "SettingsFragment";

    SwitchPreferenceCompat notificationEnabled;
    TimePreference lunchReminder;
    Preference workplaceField;
    Preference username;
    Preference deleteButton;

    public SettingsFragment() {}

    //Lunch notifications
    Preference.OnPreferenceChangeListener notificationSwitchListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(@NonNull Preference preference, Object newValue) {
            final boolean enabled = (boolean) newValue;

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

    Preference.OnPreferenceClickListener usernameClickListener = new Preference.OnPreferenceClickListener() {
        @Override
        public boolean onPreferenceClick(@NonNull Preference preference) {

            // Updates "username" PreferenceSummary
            Callback<String> summaryCallback = new Callback<String>() {
                @Override
                public void onSuccess(String username) {
                    preference.setSummary(username);
                }

                @Override
                public void onFailure() {

                }
            };
            ((HomepageActivity) getActivity()).launchUsernamePickerDialog(summaryCallback);

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
        Log.d(TAG, "onCreatePreferences");
        setPreferencesFromResource(R.xml.preferences, rootKey);
        viewsBinding();
        notificationEnabled.setOnPreferenceChangeListener(notificationSwitchListener);
        workplaceField.setSummary(workplaceField.getSharedPreferences().getString("workplace", "N/A"));
        workplaceField.setOnPreferenceClickListener(workplaceClickListener);
        username.setSummary(username.getSharedPreferences().getString("username", "N/A"));
        username.setOnPreferenceClickListener(usernameClickListener);
        deleteButton.setOnPreferenceClickListener(deleteBtnClickListener);
    }

    private void viewsBinding(){
        Log.d(TAG, "viewsBinding");
        notificationEnabled = findPreference("notification_enabled");
        lunchReminder = findPreference("lunch_reminder");
        workplaceField = findPreference("workplace");
        username = findPreference("username");
        deleteButton = findPreference("delete");
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();
        ((HomepageActivity)getActivity()).settingsFragmentDisplayOptions();
    }


    @Override
    public void onDisplayPreferenceDialog(@NonNull Preference preference) {
        Log.d(TAG, "onDisplayPreferenceDialog");
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

    private void scheduleNotificationJob(Context context, @Nullable Calendar dueDate) {
        Log.d(TAG, "scheduleNotificationJob");
        ((HomepageActivity)getActivity()).scheduleNotification(context, dueDate);
    }

    private void deleteNotificationJob(Context context) {
        Log.d(TAG, "deleteNotificationJob");
        String JOB_TAG = "GO4LUNCH_NOTIFICATION_WORKER";
        WorkManager.getInstance(context).cancelAllWorkByTag(JOB_TAG);
    }

}