package com.testarossa.template.library.android.utils

import android.os.Build
import com.testarossa.template.library.android.utils.extension.isBuildLargerThan
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Định dạng thời gian từ mili giây sang phút:giây hoặc giờ:phút:giây
 *
 * @param timeInMillis mili giây cần định dạng
 * @return thời gian đã được định dạng
 */
fun formatTime(timeInMillis: Long): String {
    val hour = TimeUnit.MILLISECONDS.toHours(timeInMillis)
    val mm = TimeUnit.MILLISECONDS.toMinutes(timeInMillis) % 60
    val ss = TimeUnit.MILLISECONDS.toSeconds(timeInMillis) % 60
    return if (hour == 0L) String.format(
        Locale.US,
        "%02d:%02d",
        mm,
        ss
    ) else String.format(Locale.US, "%02d:%02d:%02d", hour, mm, ss)
}

/**
 * Định dạng thời gian từ mili giây sang giờ:phút:giây đầy đủ
 *
 * @param timeInMillis mili giây cần định dạng
 * @return thời gian đã được định dạng
 */
fun formatFullTime(timeInMillis: Long): String {
    val hour = TimeUnit.MILLISECONDS.toHours(timeInMillis)
    val mm = TimeUnit.MILLISECONDS.toMinutes(timeInMillis) % 60
    val ss = TimeUnit.MILLISECONDS.toSeconds(timeInMillis) % 60
    return String.format(Locale.US, "%02d:%02d:%02d", hour, mm, ss)
}

fun formatTimePattern(
    timeInMillis: Long?,
    pattern: String,
    locale: Locale = Locale.getDefault()
): String {
    val sdf = SimpleDateFormat(pattern, locale)
    return sdf.format(timeInMillis ?: 0)
}

fun formatTimePattern(
    date: Date,
    pattern: String,
    locale: Locale = Locale.getDefault()
): String {
    val sdf = SimpleDateFormat(pattern, locale)
    return sdf.format(date)
}


fun getCurrentTime(): String {
    val currentTime = Calendar.getInstance().time
    val simpleDateFormat =
        SimpleDateFormat("HH:mm:ss", Locale.US)
    return simpleDateFormat.format(currentTime)
}

fun getCurrentDate(): String {
    return if (isBuildLargerThan(Build.VERSION_CODES.O)) {
        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy_HH/mm/ss")
        current.format(formatter).toString()
    } else {
        val date = Date()
        val formatter = SimpleDateFormat("dd/MM/yyyy_HH/mm/ss", Locale.US)
        formatter.format(date).toString()
    }
}
