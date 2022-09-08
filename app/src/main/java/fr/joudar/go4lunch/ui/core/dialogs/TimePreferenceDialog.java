package fr.joudar.go4lunch.ui.core.dialogs;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceDialogFragmentCompat;

import fr.joudar.go4lunch.ui.activities.HomepageActivity;

public class TimePreferenceDialog extends PreferenceDialogFragmentCompat {

    private String TAG = "TimePreferenceDialog";
    private TimePicker timePicker;

    public static TimePreferenceDialog getInstance(String preferenceKey) {
        Log.d("TimePreferenceDialog", "getInstance");
        final TimePreferenceDialog instance = new TimePreferenceDialog();
        final Bundle bundle = new Bundle();
        bundle.putString(ARG_KEY, preferenceKey);
        instance.setArguments(bundle);
        return instance;
    }

    @Nullable
    @Override
    protected View onCreateDialogView(@NonNull Context context) {
        Log.d(TAG, "onCreateDialogView");
        super.onCreateDialogView(context);
        timePicker = new TimePicker(context);
        return timePicker;
    }

    @Override
    protected void onBindDialogView(@NonNull View view) {
        Log.d(TAG, "onBindDialogView");
        super.onBindDialogView(view);
        final TimePreference timePreference = (TimePreference) getPreference();
        final int time = timePreference.getPersistedInt();
        timePicker.setIs24HourView(true);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            timePicker.setCurrentMinute(time % 60);
            timePicker.setCurrentHour(time / 60);
        } else {
            timePicker.setMinute(time % 60);
            timePicker.setHour(time / 60);
        }

    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        Log.d(TAG, "onDialogClosed");
        if (positiveResult) {
            int time;
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
                time = (timePicker.getCurrentHour() * 60) + timePicker.getCurrentMinute();
            else
                time = (timePicker.getHour() * 60) + timePicker.getMinute();
            final TimePreference timePreference = (TimePreference) getPreference();
            timePreference.setPersistedTime(time);
            ((HomepageActivity)getActivity()).scheduleNotificationJob(getContext(), timePreference.getPersistedTime());
        }
    }
}
