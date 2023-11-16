package com.example.alarmtest

import android.annotation.SuppressLint
import android.os.Build
import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator


//Fonctionnalité de vibreur
class VibratorTool(private val context: Context)  {
    // Pour obtenir le service Vibrator
    // private val vibrator: Vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    @SuppressLint("ServiceCast")
    private val vibrator: Vibrator = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as Vibrator
    fun vibrate(duration: Long) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(duration)
        }
    }
}