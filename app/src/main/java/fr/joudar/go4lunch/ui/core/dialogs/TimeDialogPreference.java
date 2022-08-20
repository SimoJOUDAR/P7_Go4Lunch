package fr.joudar.go4lunch.ui.core.dialogs;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.DialogPreference;

import java.util.Calendar;
import java.util.Locale;

public class TimeDialogPreference extends DialogPreference {

    int defaultTimeValue = 12 * 60;

    public TimeDialogPreference(@NonNull Context context) {
        super(context);
        initTimeDialogPreference();
    }

    private void initTimeDialogPreference() {
        setPositiveButtonText("Set");
        setNegativeButtonText("Cancel");
        setTimeFormatSummary();
    }

    @Override
    protected void onSetInitialValue(@Nullable Object defaultValue) {
        super.onSetInitialValue(defaultValue);
        setTimeFormatSummary();
    }

    private void setTimeFormatSummary(){
        //setSummary(DateTimeFormatterBuilder.getLocalizedDateTimePattern(FormatStyle.SHORT,null, IsoChronology.INSTANCE, Locale.getDefault()));
        setSummary(String.format(Locale.getDefault(), "%1$02d:%2$02d", getPersistedInt() / 60, getPersistedInt() % 60));
    }

    // Here "time" is the number of minutes since 00h00
    public void setPersistedTime(int time) {
        persistInt(time);
        setTimeFormatSummary();
        notifyChanged();
    }

    //
    public Calendar getPersistedTime(){
        final Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, getPersistedInt() / 60);
        calendar.set(Calendar.MINUTE, getPersistedInt() % 60);
        if (calendar.before(Calendar.getInstance()))
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        return calendar;
    }

    public int getPersistedInt() {
        return getPersistedInt(defaultTimeValue);
    }

}
