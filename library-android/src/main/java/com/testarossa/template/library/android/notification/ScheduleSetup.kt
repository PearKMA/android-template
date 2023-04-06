package com.testarossa.template.library.android.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.util.Calendar

fun Context.setAlarm() {
    val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
    scheduleAlarmDaily(alarmManager)
    scheduleAlarm(alarmManager, Calendar.MONDAY)
    scheduleAlarm(alarmManager, Calendar.TUESDAY)
    scheduleAlarm(alarmManager, Calendar.WEDNESDAY)
    scheduleAlarm(alarmManager, Calendar.THURSDAY)
    scheduleAlarm(alarmManager, Calendar.FRIDAY)
    scheduleAlarm(alarmManager, Calendar.SATURDAY, secondHour = 11, thirdHour = 19)
    scheduleAlarm(alarmManager, Calendar.SUNDAY, secondHour = 11, thirdHour = 19)
}

fun Context.scheduleAlarmDaily(alarmManager: AlarmManager) {
    val calendar15h = Calendar.getInstance().apply {
        timeInMillis = System.currentTimeMillis()
        set(Calendar.HOUR_OF_DAY, 15)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
    }
    val current = Calendar.getInstance()

    if (current.after(calendar15h)) {
        calendar15h.add(Calendar.DATE, 1)
    }

    val intent15h = Intent(this, ScheduleReceiver::class.java).let { intent ->
        PendingIntent.getBroadcast(
            this,
            1500,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    alarmManager.setRepeating(
        AlarmManager.RTC_WAKEUP,
        calendar15h.timeInMillis,
        AlarmManager.INTERVAL_DAY,
        intent15h
    )
}

fun Context.scheduleAlarm(
    alarmManager: AlarmManager,
    dayOfWeek: Int,
    hour: Int = 9,
    secondHour: Int? = null,
    thirdHour: Int? = null
) {
    val calendar = Calendar.getInstance().apply {
        timeInMillis = System.currentTimeMillis()
        set(Calendar.DAY_OF_WEEK, dayOfWeek)
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
    }
    val current = Calendar.getInstance()
    if (current.after(calendar)) {
        calendar.add(Calendar.DATE, 7)
    }

    val intentFirst = Intent(this, ScheduleReceiver::class.java).let { intent ->
        PendingIntent.getBroadcast(
            this,
            dayOfWeek * hour,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    alarmManager.setRepeating(
        AlarmManager.RTC_WAKEUP,
        calendar.timeInMillis,
        AlarmManager.INTERVAL_DAY,
        intentFirst
    )
    if (secondHour != null) {
        val secondCalendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.DAY_OF_WEEK, dayOfWeek)
            set(Calendar.HOUR_OF_DAY, secondHour)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }
        if (current.after(secondCalendar)) {
            secondCalendar.add(Calendar.DATE, 7)
        }
        val intentSecond = Intent(this, ScheduleReceiver::class.java).let { intent ->
            PendingIntent.getBroadcast(
                this,
                dayOfWeek * secondHour,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        }

        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            secondCalendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            intentSecond
        )
    }
    if (thirdHour != null) {
        val thirdCalendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.DAY_OF_WEEK, dayOfWeek)
            set(Calendar.HOUR_OF_DAY, thirdHour)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }
        if (current.after(thirdCalendar)) {
            thirdCalendar.add(Calendar.DATE, 7)
        }
        val intentThird = Intent(this, ScheduleReceiver::class.java).let { intent ->
            PendingIntent.getBroadcast(
                this,
                dayOfWeek * thirdHour,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        }

        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            thirdCalendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            intentThird
        )
    }
}