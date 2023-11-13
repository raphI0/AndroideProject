package com.example.alarmtest

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AlarmBroadcastReceiver: BroadcastReceiver() {
    override fun onReceive(p0: Context?, p1: Intent?) {
        val message = p1?.getStringExtra("EXTRA_MESSAGE") ?: return
        println("Alarm triggered : $message")
    }
}