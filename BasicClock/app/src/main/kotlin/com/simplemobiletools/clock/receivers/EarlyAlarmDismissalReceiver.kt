package com.simplemobiletools.clock.receivers

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.simplemobiletools.clock.R
import com.simplemobiletools.clock.extensions.getClosestEnabledAlarmString
import com.simplemobiletools.clock.extensions.getDismissAlarmPendingIntent
import com.simplemobiletools.clock.extensions.getOpenAlarmTabIntent
import com.simplemobiletools.clock.helpers.ALARM_ID
import com.simplemobiletools.clock.helpers.EARLY_ALARM_DISMISSAL_CHANNEL_ID
import com.simplemobiletools.clock.helpers.EARLY_ALARM_NOTIF_ID
import com.simplemobiletools.commons.helpers.isOreoPlus

class EarlyAlarmDismissalReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getIntExtra(ALARM_ID, -1)
        if (alarmId == -1) {
            return
        }

        triggerEarlyDismissalNotification(context, alarmId)
    }

    private fun triggerEarlyDismissalNotification(context: Context, alarmId: Int) {
        context.getClosestEnabledAlarmString { alarmString ->
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (isOreoPlus()) {
                NotificationChannel(
                    EARLY_ALARM_DISMISSAL_CHANNEL_ID,
                    context.getString(R.string.early_alarm_dismissal),
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    setBypassDnd(true)
                    setSound(null, null)
                    notificationManager.createNotificationChannel(this)
                }
            }
            val dismissIntent = context.getDismissAlarmPendingIntent(alarmId, EARLY_ALARM_NOTIF_ID)
            val contentIntent = context.getOpenAlarmTabIntent()
            val notification = NotificationCompat.Builder(context)
                .setContentTitle(context.getString(R.string.upcoming_alarm))
                .setContentText(alarmString)
                .setSmallIcon(R.drawable.ic_alarm_vector)
                .setPriority(Notification.PRIORITY_LOW)
                .addAction(0, context.getString(com.simplemobiletools.commons.R.string.dismiss), dismissIntent)
                .setContentIntent(contentIntent)
                .setSound(null)
                .setAutoCancel(true)
                .setChannelId(EARLY_ALARM_DISMISSAL_CHANNEL_ID)
                .build()

            notificationManager.notify(EARLY_ALARM_NOTIF_ID, notification)
        }
    }

}
