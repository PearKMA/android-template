package com.testarossa.template.library.android.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.testarossa.template.library.android.R
import com.testarossa.template.library.android.utils.extension.buildVersion
import com.testarossa.template.library.android.utils.extension.isBuildLargerThan

class ScheduleReceiver : BroadcastReceiver() {

    private val channelId = "base_schedule_channel"

    override fun onReceive(p0: Context?, p1: Intent?) {
        if (p0 == null) return

        val notificationManager =
            p0.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (isBuildLargerThan(buildVersion.O)) {
            val name = p0.getString(R.string.channel_name)
            val descriptionText = p0.getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            notificationManager.createNotificationChannel(channel)
        }

        /*val notificationIntent = Intent(p0, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            p0, 0,
            notificationIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)


        val stringResource = p0.getContentNotify()

        val builder = NotificationCompat.Builder(p0, channelId)
            .setSmallIcon(R.mipmap.logo)
            .setContentTitle(stringResource.first)
            .setContentText(stringResource.second)
            .setSound(alarmSound)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setStyle(NotificationCompat.BigTextStyle().bigText(stringResource.second))
            .setContentIntent(pendingIntent)

        with(NotificationManagerCompat.from(p0)) {
            notify(5, builder.build())
        }*/
    }

}