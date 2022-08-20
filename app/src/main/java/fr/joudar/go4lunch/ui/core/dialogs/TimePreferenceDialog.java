package fr.joudar.go4lunch.ui.core.dialogs;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceDialogFragmentCompat;

import java.util.Calendar;

import fr.joudar.go4lunch.ui.activities.HomepageActivity;

public class TimePreferenceDialog extends PreferenceDialogFragmentCompat {

    private TimePicker timePicker;

    public static TimePreferenceDialog getInstance(String preferenceKey) {
        final TimePreferenceDialog instance = new TimePreferenceDialog();
        final Bundle bundle = new Bundle();
        bundle.putString(ARG_KEY, preferenceKey);
        instance.setArguments(bundle);
        return instance;
    }

    @Nullable
    @Override
    protected View onCreateDialogView(@NonNull Context context) {
        super.onCreateDialogView(context);
        timePicker = new TimePicker(context);
        return timePicker;
    }

    @Override
    protected void onBindDialogView(@NonNull View view) {
        super.onBindDialogView(view);
        final TimeDialogPreference timeDialogPreference = (TimeDialogPreference) getPreference();
        final int time = timeDialogPreference.getPersistedInt();
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
        if (positiveResult) {
            int time;
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
                time = (timePicker.getCurrentHour() * 60) + timePicker.getCurrentMinute();
            else
                time = (timePicker.getHour() * 60) + timePicker.getMinute();
            final TimeDialogPreference timeDialogPreference = (TimeDialogPreference) getPreference();
            timeDialogPreference.setPersistedTime(time);
            ((HomepageActivity)getActivity()).scheduleNotificationJob(getContext(), timeDialogPreference.getPersistedTime());
        }
    }
}
