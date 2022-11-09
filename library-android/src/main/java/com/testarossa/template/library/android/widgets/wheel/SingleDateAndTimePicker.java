package com.testarossa.template.library.android.widgets.wheel;
// Recycle later (maybe （￣︶￣）↗　)
/*

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;

import com.testarossa.template.library.android.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

*/
/**
 * https://github.com/florent37/SingleDateAndTimePicker
 *//*

public class SingleDateAndTimePicker extends LinearLayout {
    public static final boolean IS_CYCLIC_DEFAULT = true;
    public static final boolean IS_CURVED_DEFAULT = false;
    public static final int DELAY_BEFORE_CHECK_PAST = 200;
    public static final int ALIGN_CENTER = 0;
    private static final int VISIBLE_ITEM_COUNT_DEFAULT = 7;
    private static final CharSequence FORMAT_24_HOUR = "EEE d MMM H:mm";
    @NonNull
    private final WheelMinutePicker minutesPicker;
    private final WheelSecondPicker secondsPicker;
    private final List<WheelPicker> pickers = new ArrayList<>();
    private final List<OnDateChangedListener> listeners = new ArrayList<>();
    private final View dtSelector;
    private DateHelper dateHelper = new DateHelper();
    @Nullable
    private Date minDate;
    @Nullable
    private Date maxDate;
    @NonNull
    private Date defaultDate;


    public SingleDateAndTimePicker(Context context) {
        this(context, null);
    }

    public SingleDateAndTimePicker(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SingleDateAndTimePicker(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        defaultDate = new Date();

        inflate(context, R.layout.time_picker, this);

        minutesPicker = findViewById(R.id.minutesPicker);
        secondsPicker = findViewById(R.id.secondsPicker);
        dtSelector = findViewById(R.id.dtSelector);

        minutesPicker.setMaxMinutes(4);
        pickers.addAll(Arrays.asList(
                minutesPicker,
                secondsPicker
        ));
        for (WheelPicker wheelPicker : pickers) {
            wheelPicker.setDateHelper(dateHelper);
        }
        init(context, attrs);
    }

    public void setDateHelper(DateHelper dateHelper) {
        this.dateHelper = dateHelper;
    }

    public void setTimeZone(TimeZone timeZone) {
        dateHelper.setTimeZone(timeZone);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        minutesPicker
                .setOnMinuteChangedListener((picker, minutes) -> {
                    updateListener();
                    checkMinMaxDate(picker);
                })
                .setOnFinishedLoopListener(picker -> {
                });
        secondsPicker
                .setOnSecondChangedListener((picker, minutes) -> {
                    updateListener();
                    checkMinMaxDate(picker);
                })
                .setOnFinishedLoopListener(picker -> minutesPicker.scrollTo(minutesPicker.getCurrentItemPosition() + 1));

        setDefaultDate(this.defaultDate); //update displayed date
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        for (WheelPicker picker : pickers) {
            picker.setEnabled(enabled);
        }
    }

    public void setItemSpacing(int size) {
        for (WheelPicker picker : pickers) {
            picker.setItemSpace(size);
        }
    }

    public void setCurvedMaxAngle(int angle) {
        for (WheelPicker picker : pickers) {
            picker.setCurvedMaxAngle(angle);
        }
    }

    public void setCurved(boolean curved) {
        for (WheelPicker picker : pickers) {
            picker.setCurved(curved);
        }
    }

    public void setCyclic(boolean cyclic) {
        for (WheelPicker picker : pickers) {
            picker.setCyclic(cyclic);
        }
    }

    public void setTextSize(int textSize) {
        for (WheelPicker picker : pickers) {
            picker.setItemTextSize(textSize);
        }
    }

    public void setSelectedTextColor(int selectedTextColor) {
        for (WheelPicker picker : pickers) {
            picker.setSelectedItemTextColor(selectedTextColor);
        }
    }

    public void setTextColor(int textColor) {
        for (WheelPicker picker : pickers) {
            picker.setItemTextColor(textColor);
        }
    }

    public void setTextAlign(int align) {
        for (WheelPicker picker : pickers) {
            picker.setItemAlign(align);
        }
    }

    public void setTypeface(Typeface typeface) {
        if (typeface == null) return;
        for (WheelPicker picker : pickers) {
            picker.setTypeface(typeface);
        }
    }

    private void setFontToAllPickers(int resourceId) {
        if (resourceId > 0) {
            for (int i = 0; i < pickers.size(); i++) {
                pickers.get(i).setTypeface(ResourcesCompat.getFont(getContext(), resourceId));
            }
        }
    }

    public void setSelectorColor(int selectorColor) {
        dtSelector.setBackgroundColor(selectorColor);
    }

    public void setSelectorHeight(int selectorHeight) {
        final ViewGroup.LayoutParams dtSelectorLayoutParams = dtSelector.getLayoutParams();
        dtSelectorLayoutParams.height = selectorHeight;
        dtSelector.setLayoutParams(dtSelectorLayoutParams);
    }

    public void setVisibleItemCount(int visibleItemCount) {
        for (WheelPicker picker : pickers) {
            picker.setVisibleItemCount(visibleItemCount);
        }
    }

    public Date getMinDate() {
        return minDate;
    }

    public void setMinDate(Date minDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(dateHelper.getTimeZone());
        calendar.setTime(minDate);
        this.minDate = calendar.getTime();
    }

    public Date getMaxDate() {
        return maxDate;
    }

    public void setMaxDate(Date maxDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(dateHelper.getTimeZone());
        calendar.setTime(maxDate);
        this.maxDate = calendar.getTime();
    }

    public void setCustomLocale(Locale locale) {
        for (WheelPicker p : pickers) {
            p.setCustomLocale(locale);
            p.updateAdapter();
        }
    }

    private void checkMinMaxDate(final WheelPicker picker) {
        checkBeforeMinDate(picker);
        checkAfterMaxDate(picker);
    }

    private void checkBeforeMinDate(final WheelPicker picker) {
        picker.postDelayed(() -> {
            if (minDate != null && isBeforeMinDate(getDate())) {
                for (WheelPicker p : pickers) {
                    p.scrollTo(p.findIndexOfDate(minDate));
                }
            }
        }, DELAY_BEFORE_CHECK_PAST);
    }

    private void checkAfterMaxDate(final WheelPicker picker) {
        picker.postDelayed(() -> {
            if (maxDate != null && isAfterMaxDate(getDate())) {
                for (WheelPicker p : pickers) {
                    p.scrollTo(p.findIndexOfDate(maxDate));
                }
            }
        }, DELAY_BEFORE_CHECK_PAST);
    }

    private boolean isBeforeMinDate(Date date) {
        return dateHelper.getCalendarOfDate(date).before(dateHelper.getCalendarOfDate(minDate));
    }

    private boolean isAfterMaxDate(Date date) {
        return dateHelper.getCalendarOfDate(date).after(dateHelper.getCalendarOfDate(maxDate));
    }

    public void addOnDateChangedListener(OnDateChangedListener listener) {
        this.listeners.add(listener);
    }

    public void removeOnDateChangedListener(OnDateChangedListener listener) {
        this.listeners.remove(listener);
    }

    public void checkPickersMinMax() {
        for (WheelPicker picker : pickers) {
            checkMinMaxDate(picker);
        }
    }

    public Date getDate() {
        final int minute = minutesPicker.getCurrentMinute();
        final int second = secondsPicker.getCurrentSecond();

        final Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(dateHelper.getTimeZone());
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, second);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    public void setStepSizeMinutes(int minutesStep) {
        minutesPicker.setStepSizeMinutes(minutesStep);
    }

    public void setStepSizeSeconds(int minutesStep) {
        secondsPicker.setStepSizeSeconds(minutesStep);
    }

    public void setDefaultValue() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(dateHelper.getTimeZone());
        calendar.setTime(new Date());
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        this.defaultDate = calendar.getTime();

        for (WheelPicker picker : pickers) {
            picker.setDefaultDate(defaultDate);
        }
    }

    public void setDefaultDate(Date date) {
        if (date != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeZone(dateHelper.getTimeZone());
            calendar.setTime(date);
            this.defaultDate = calendar.getTime();

            for (WheelPicker picker : pickers) {
                picker.setDefaultDate(defaultDate);
            }
        }
    }

    public void selectDate(Calendar calendar) {
        if (calendar == null) {
            return;
        }

        final Date date = calendar.getTime();
        for (WheelPicker picker : pickers) {
            picker.selectDate(date);
        }

    }

    private void updateListener() {
        final Date date = getDate();
        final String displayed = DateFormat.format(FORMAT_24_HOUR, date).toString();
        for (OnDateChangedListener listener : listeners) {
            listener.onDateChanged(displayed, date);
        }
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SingleDateAndTimePicker);

        final Resources resources = getResources();
        setTextColor(a.getColor(R.styleable.SingleDateAndTimePicker_picker_textColor, Color.parseColor("#9905284D")));
        setSelectedTextColor(a.getColor(R.styleable.SingleDateAndTimePicker_picker_selectedTextColor, Color.parseColor("#05284D")));
        setSelectorColor(a.getColor(R.styleable.SingleDateAndTimePicker_picker_selectorColor, Color.parseColor("#F6F6FF")));
        setItemSpacing(a.getDimensionPixelSize(R.styleable.SingleDateAndTimePicker_picker_itemSpacing, resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._4sdp)));
        setCurvedMaxAngle(a.getInteger(R.styleable.SingleDateAndTimePicker_picker_curvedMaxAngle, WheelPicker.MAX_ANGLE));
        setSelectorHeight(a.getDimensionPixelSize(R.styleable.SingleDateAndTimePicker_picker_selectorHeight, resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._28sdp)));
        setTextSize(a.getDimensionPixelSize(R.styleable.SingleDateAndTimePicker_picker_textSize, resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._14sdp)));
        setCurved(a.getBoolean(R.styleable.SingleDateAndTimePicker_picker_curved, IS_CURVED_DEFAULT));
        setCyclic(a.getBoolean(R.styleable.SingleDateAndTimePicker_picker_cyclic, IS_CYCLIC_DEFAULT));
        setVisibleItemCount(a.getInt(R.styleable.SingleDateAndTimePicker_picker_visibleItemCount, VISIBLE_ITEM_COUNT_DEFAULT));

        setStepSizeMinutes(a.getInt(R.styleable.SingleDateAndTimePicker_picker_stepSizeMinutes, 1));
        setStepSizeSeconds(a.getInt(R.styleable.SingleDateAndTimePicker_picker_stepSizeMinutes, 1));

        setFontToAllPickers(a.getResourceId(R.styleable.SingleDateAndTimePicker_fontFamily, 0));
        setTextAlign(a.getInt(R.styleable.SingleDateAndTimePicker_picker_textAlign, ALIGN_CENTER));


        a.recycle();
    }

    public interface OnDateChangedListener {
        void onDateChanged(String displayed, Date date);
    }
}
*/
