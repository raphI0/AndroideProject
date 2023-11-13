package com.example.alarmtest

interface AlarmScheduler {
    fun schedule(item:AlarmItem)
    fun cancel(item:AlarmItem)
}