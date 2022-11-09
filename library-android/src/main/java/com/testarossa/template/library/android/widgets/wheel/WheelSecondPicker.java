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

public class WheelSecondPicker extends WheelPicker<String> {
    public static final int MIN_SECONDS = 0;
    public static final int MAX_SECONDS = 59;
    public static final int STEP_SECONDS_DEFAULT = 1;
    private int stepSeconds;
    private int minSeconds;
    private int maxSeconds;

    private OnSecondChangedListener onSecondChangedListener;
    private OnFinishedLoopListener onFinishedLoopListener;

    public WheelSecondPicker(Context context) {
        super(context);
    }

    public WheelSecondPicker(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void init() {
        stepSeconds = STEP_SECONDS_DEFAULT;
        minSeconds = MIN_SECONDS;
        maxSeconds = MAX_SECONDS;
    }

    @Override
    protected String initDefault() {
        Calendar now = Calendar.getInstance();
        now.setTimeZone(dateHelper.getTimeZone());
        now.set(Calendar.SECOND, 0);
        return getFormattedValue(now.get(Calendar.SECOND));
    }

    @Override
    protected List<String> generateAdapterValues(boolean showOnlyFutureDates) {
        final List<String> seconds = new ArrayList<>();
        for (int second = minSeconds; second <= maxSeconds; second += stepSeconds) {
            seconds.add(getFormattedValue(second));
        }
        return seconds;
    }

    private int findIndexOfSecond(int second) {
        final int itemCount = adapter.getItemCount();
        for (int i = 0; i < itemCount; ++i) {
            final String object = adapter.getItemText(i);
            final int value = Integer.parseInt(object);

            if (second == value) {
                return i;
            }

            if (second < value) {
                return i - 1;
            }
        }
        return itemCount - 1;

    }

    @Override
    public int findIndexOfDate(@NonNull Date date) {
        return findIndexOfSecond(dateHelper.getSecondOf(date));
    }

    protected String getFormattedValue(Object value) {
        Object valueItem = value;
        if (value instanceof Date) {
            final Calendar instance = Calendar.getInstance();
            instance.setTimeZone(dateHelper.getTimeZone());
            instance.setTime((Date) value);
            valueItem = instance.get(Calendar.SECOND);
        }
        return String.format(getCurrentLocale(), FORMAT, valueItem);
    }

    public void setStepSizeSeconds(int stepSeconds) {
        if (stepSeconds < 60 && stepSeconds > 0) {
            this.stepSeconds = stepSeconds;
            updateAdapter();
        }
    }

    public void setMinSeconds(int minSeconds) {
        if (minSeconds < 60 && minSeconds >= 0) {
            this.minSeconds = minSeconds;
            updateAdapter();
        }
    }

    public void setMaxSeconds(int maxSeconds) {
        if (maxSeconds < 60 && maxSeconds > 0) {
            this.maxSeconds = maxSeconds;
            updateAdapter();
        }
    }

    private int convertItemToSecond(Object item) {
        return Integer.parseInt(String.valueOf(item));
    }

    public int getCurrentSecond() {
        return convertItemToSecond(adapter.getItem(getCurrentItemPosition()));
    }

    public WheelSecondPicker setOnSecondChangedListener(OnSecondChangedListener onSecondChangedListener) {
        this.onSecondChangedListener = onSecondChangedListener;
        return this;
    }

    public WheelSecondPicker setOnFinishedLoopListener(OnFinishedLoopListener onFinishedLoopListener) {
        this.onFinishedLoopListener = onFinishedLoopListener;
        return this;
    }

    @Override
    protected void onItemSelected(int position, String item) {
        super.onItemSelected(position, item);
        if (onSecondChangedListener != null) {
            onSecondChangedListener.onSecondChanged(this, convertItemToSecond(item));
        }
    }

    @Override
    protected void onFinishedLoop() {
        super.onFinishedLoop();
        if (onFinishedLoopListener != null) {
            onFinishedLoopListener.onFinishedLoop(this);
        }
    }

    public interface OnSecondChangedListener {
        void onSecondChanged(WheelSecondPicker picker, int seconds);
    }

    public interface OnFinishedLoopListener {
        void onFinishedLoop(WheelSecondPicker picker);
    }
}
*/
