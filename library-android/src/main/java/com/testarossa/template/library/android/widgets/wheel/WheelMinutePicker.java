package com.testarossa.template.library.android.widgets.wheel;
// Recycle later (maybe （￣︶￣）↗　)
/*
import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class WheelMinutePicker extends WheelPicker<String> {
    public static final int MIN_MINUTES = 0;
    public static final int MAX_MINUTES = 59;
    public static final int STEP_MINUTES_DEFAULT = 5;
    private int stepMinutes;
    private int minMinutes;
    private int maxMinutes;

    private OnMinuteChangedListener onMinuteChangedListener;
    private OnFinishedLoopListener onFinishedLoopListener;

    public WheelMinutePicker(Context context) {
        super(context);
    }

    public WheelMinutePicker(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void init() {
        stepMinutes = STEP_MINUTES_DEFAULT;
        minMinutes = MIN_MINUTES;
        maxMinutes = MAX_MINUTES;
    }

    @Override
    protected String initDefault() {
        Calendar now = Calendar.getInstance();
        now.setTimeZone(dateHelper.getTimeZone());
        now.set(Calendar.MINUTE, 0);
        return getFormattedValue(now.get(Calendar.MINUTE));
    }

    @Override
    protected List<String> generateAdapterValues(boolean showOnlyFutureDates) {
        final List<String> minutes = new ArrayList<>();
        for (int min = minMinutes; min <= maxMinutes; min += stepMinutes) {
            minutes.add(getFormattedValue(min));
        }
        return minutes;
    }

    private int findIndexOfMinute(int minute) {
        final int itemCount = adapter.getItemCount();
        for (int i = 0; i < itemCount; ++i) {
            final String object = adapter.getItemText(i);
            final int value = Integer.parseInt(object);

            if (minute == value) {
                return i;
            }

            if (minute < value) {
                return i - 1;
            }
        }
        return itemCount - 1;

    }

    @Override
    public int findIndexOfDate(@NonNull Date date) {
        return findIndexOfMinute(dateHelper.getMinuteOf(date));
    }

    protected String getFormattedValue(Object value) {
        Object valueItem = value;
        if (value instanceof Date) {
            final Calendar instance = Calendar.getInstance();
            instance.setTimeZone(dateHelper.getTimeZone());
            instance.setTime((Date) value);
            valueItem = instance.get(Calendar.MINUTE);
        }
        return String.format(getCurrentLocale(), FORMAT, valueItem);
    }

    public void setStepSizeMinutes(int stepMinutes) {
        if (stepMinutes < 60 && stepMinutes > 0) {
            this.stepMinutes = stepMinutes;
            updateAdapter();
        }
    }

    public void setMinMinutes(int minMinutes) {
        if (minMinutes < 60 && minMinutes >= 0) {
            this.minMinutes = minMinutes;
            updateAdapter();
        }
    }

    public void setMaxMinutes(int maxMinutes) {
        if (maxMinutes < 60 && maxMinutes > 0) {
            this.maxMinutes = maxMinutes;
            updateAdapter();
        }
    }

    private int convertItemToMinute(Object item) {
        return Integer.parseInt(String.valueOf(item));
    }

    public int getCurrentMinute() {
        return convertItemToMinute(adapter.getItem(getCurrentItemPosition()));
    }

    public WheelMinutePicker setOnMinuteChangedListener(OnMinuteChangedListener onMinuteChangedListener) {
        this.onMinuteChangedListener = onMinuteChangedListener;
        return this;
    }

    public WheelMinutePicker setOnFinishedLoopListener(OnFinishedLoopListener onFinishedLoopListener) {
        this.onFinishedLoopListener = onFinishedLoopListener;
        return this;
    }

    @Override
    protected void onItemSelected(int position, String item) {
        super.onItemSelected(position, item);
        if (onMinuteChangedListener != null) {
            onMinuteChangedListener.onMinuteChanged(this, convertItemToMinute(item));
        }
    }

    @Override
    protected void onFinishedLoop() {
        super.onFinishedLoop();
        if (onFinishedLoopListener != null) {
            onFinishedLoopListener.onFinishedLoop(this);
        }
    }

    public interface OnMinuteChangedListener {
        void onMinuteChanged(WheelMinutePicker picker, int minutes);
    }

    public interface OnFinishedLoopListener {
        void onFinishedLoop(WheelMinutePicker picker);
    }
}*/
