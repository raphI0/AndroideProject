package com.simplemobiletools.clock.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.simplemobiletools.clock.extensions.*
import com.simplemobiletools.clock.helpers.ALARM_ID
import com.simplemobiletools.clock.helpers.NOTIFICATION_ID
import com.simplemobiletools.clock.models.Alarm
import com.simplemobiletools.commons.extensions.removeBit
import com.simplemobiletools.commons.helpers.ensureBackgroundThread
import java.util.Calendar
import kotlin.math.pow

class DismissAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getIntExtra(ALARM_ID, -1)
        val notificationId = intent.getIntExtra(NOTIFICATION_ID, -1)
        if (alarmId == -1) {
            return
        }

        context.hideNotification(notificationId)

        ensureBackgroundThread {
            context.dbHelper.getAlarmWithId(alarmId)?.let { alarm ->
                context.cancelAlarmClock(alarm)
                scheduleNextAlarm(alarm, context)
                if (alarm.days < 0) {
                    if (alarm.oneShot) {
                        alarm.isEnabled = false
                        context.dbHelper.deleteAlarms(arrayListOf(alarm))
                    } else {
                        context.dbHelper.updateAlarmEnabledState(alarm.id, false)
                    }
                    context.updateWidgets()
                }
            }
        }
    }

    private fun scheduleNextAlarm(alarm: Alarm, context: Context) {
        val oldBitmask = alarm.days
        alarm.days = removeTodayFromBitmask(oldBitmask)
        context.scheduleNextAlarm(alarm, false)
        alarm.days = oldBitmask
    }

    private fun removeTodayFromBitmask(bitmask: Int): Int {
        val calendar = Calendar.getInstance()
        calendar.firstDayOfWeek = Calendar.MONDAY
        val dayOfWeek = (calendar.get(Calendar.DAY_OF_WEEK) + 5) % 7
        val todayBitmask = 2.0.pow(dayOfWeek).toInt()
        return bitmask.removeBit(todayBitmask)
    }
}
