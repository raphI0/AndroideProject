package com.simplemobiletools.clock.activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.*
import android.provider.AlarmClock
import android.view.Display
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.widget.Button
import com.simplemobiletools.clock.R
import com.simplemobiletools.clock.databinding.ActivityReminderBinding
import com.simplemobiletools.clock.extensions.*
import com.simplemobiletools.clock.helpers.ALARM_ID
import com.simplemobiletools.clock.helpers.ALARM_NOTIF_ID
import com.simplemobiletools.clock.helpers.getPassedSeconds
import com.simplemobiletools.clock.models.Alarm
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.helpers.*
import kotlin.random.Random


class ReminderActivity : SimpleActivity() {
    companion object {
        private const val MIN_ALARM_VOLUME_FOR_INCREASING_ALARMS = 1
        private const val INCREASE_VOLUME_DELAY = 300L
    }

    private val increaseVolumeHandler = Handler(Looper.getMainLooper())
    private val maxReminderDurationHandler = Handler(Looper.getMainLooper())
    private val swipeGuideFadeHandler = Handler()
    private val vibrationHandler = Handler(Looper.getMainLooper())
    private var isAlarmReminder = false
    private var didVibrate = false
    private var wasAlarmSnoozed = false
    private var alarm: Alarm? = null
    private var audioManager: AudioManager? = null
    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private var initialAlarmVolume: Int? = null
    private var dragDownX = 0f
    private val binding: ActivityReminderBinding by viewBinding(ActivityReminderBinding::inflate)
    private var finished = false
    private var isEnigmaSolved = false


    override fun onCreate(savedInstanceState: Bundle?) {
        isMaterialActivity = true
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        showOverLockscreen()
        updateTextColors(binding.root)
        updateStatusbarColor(getProperBackgroundColor())

        val id = intent.getIntExtra(ALARM_ID, -1)
        isAlarmReminder = id != -1
        if (id != -1) {
            alarm = dbHelper.getAlarmWithId(id) ?: return
        }

        val label = if (isAlarmReminder) {
            if (alarm!!.label.isEmpty()) {
                getString(com.simplemobiletools.commons.R.string.alarm)
            } else {
                alarm!!.label
            }
        } else {
            getString(R.string.timer)
        }

        binding.reminderTitle.text = label
        binding.reminderText.text = if (isAlarmReminder) getFormattedTime(getPassedSeconds(), false, false) else getString(R.string.time_expired)

        val maxDuration = if (isAlarmReminder) config.alarmMaxReminderSecs else config.timerMaxReminderSecs
        maxReminderDurationHandler.postDelayed({
            finishActivity()
        }, maxDuration * 1000L)

        setupButtons()
        setupEffects()
    }

    private fun setupButtons() {
        if (isAlarmReminder) {
            setupAlarmButtons()
        } else {
            setupTimerButtons()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupAlarmButtons() {
        binding.reminderStop.beGone()
        binding.reminderDraggableBackground.startAnimation(AnimationUtils.loadAnimation(this, R.anim.pulsing_animation))
        binding.reminderDraggableBackground.applyColorFilter(getProperPrimaryColor())

        val textColor = getProperTextColor()
        binding.reminderDismiss.applyColorFilter(textColor)
        binding.reminderDraggable.applyColorFilter(textColor)
        binding.reminderSnooze.applyColorFilter(textColor)

        var minDragX = 0f
        var maxDragX = 0f
        var initialDraggableX = 0f

        binding.reminderDismiss.onGlobalLayout {
            minDragX = binding.reminderSnooze.left.toFloat()
            maxDragX = binding.reminderDismiss.left.toFloat()
            initialDraggableX = binding.reminderDraggable.left.toFloat()
        }

        if(!config.isEnigmaEnabled)
            isEnigmaSolved = true
        else if (config.isEnigmaEnabled)
            triggerEnigma()

        binding.reminderDraggable.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    dragDownX = event.x
                    binding.reminderDraggableBackground.animate().alpha(0f)
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    dragDownX = 0f
                    if (!didVibrate) {
                        if (isEnigmaSolved) {
                            binding.reminderDraggable.performHapticFeedback()
                            didVibrate = true
                            finishActivity()
                        } else {
                            // L'énigme n'est pas résolue, effectuez des actions supplémentaires si nécessaire
                        }
                        binding.reminderDraggable.animate().x(initialDraggableX).withEndAction {
                            binding.reminderDraggableBackground.animate().alpha(0.2f)
                        }

                        binding.reminderGuide.animate().alpha(1f).start()
                        swipeGuideFadeHandler.removeCallbacksAndMessages(null)
                        swipeGuideFadeHandler.postDelayed({
                            binding.reminderGuide.animate().alpha(0f).start()
                        }, 2000L)
                    }
                }

                MotionEvent.ACTION_MOVE -> {
                    if (isEnigmaSolved) {
                        binding.reminderDraggable.x = Math.min(maxDragX, Math.max(minDragX, event.rawX - dragDownX))
                        if (binding.reminderDraggable.x >= maxDragX - 50f) {
                            // Logique de snooze normale ici...
                        }
                    } else {
                        // L'énigme n'est pas résolue, ne pas permettre le snoozing
                        // Vous pouvez effectuer des actions supplémentaires ici, comme afficher un message à l'utilisateur
                    }
                }
            }
            true
        }
    }

    private fun triggerEnigma() {
        val enigmaButton = findViewById<Button>(R.id.enigmaButton)  // Remplacez par l'ID ou la référence réelle de votre élément
        enigmaButton.visibility = View.VISIBLE

        fun moveButtonPeriodically() {
            val handler = Handler(Looper.getMainLooper())
            handler.postDelayed(object : Runnable {
                override fun run() {
                    if (!isEnigmaSolved) {
                        // Génère une position aléatoire
                        val randomX = Random.nextInt(-500, 500) // Ajustez la plage selon vos besoins
                        val randomY = Random.nextInt(-800, 800) // Ajustez la plage selon vos besoins

                        // Déplace le bouton vers la position aléatoire
                        enigmaButton.animate().translationX(randomX.toFloat()).translationY(randomY.toFloat()).start()

                        // Répète l'opération toutes les 200 ms
                        handler.postDelayed(this, 800)
                    }
                }
            }, 200)
        }
        moveButtonPeriodically()

        enigmaButton.setOnClickListener {
            // L'utilisateur a trouvé le bouton, résoudre l'énigme
            isEnigmaSolved = true
            enigmaButton.visibility = View.GONE
            // Vous pouvez également effectuer d'autres actions ici, comme afficher un message de confirmation
        }
    }

    private fun setupTimerButtons() {
        binding.reminderStop.background = resources.getColoredDrawableWithColor(R.drawable.circle_background_filled, getProperPrimaryColor())
        arrayOf(binding.reminderSnooze, binding.reminderDraggableBackground, binding.reminderDraggable, binding.reminderDismiss).forEach {
            it.beGone()
        }

        binding.reminderStop.setOnClickListener {
            finishActivity()
        }
    }

    private fun setupEffects() {
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        initialAlarmVolume = audioManager?.getStreamVolume(AudioManager.STREAM_ALARM) ?: 7

        val doVibrate = alarm?.vibrate ?: config.timerVibrate
        if (doVibrate && isOreoPlus()) {
            val pattern = LongArray(2) { 500 }
            vibrationHandler.postDelayed({
                vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
            }, 500)
        }

        val soundUri = if (alarm != null) {
            alarm!!.soundUri
        } else {
            config.timerSoundUri

        }

        if (soundUri != SILENT) {
            try {
                mediaPlayer = MediaPlayer().apply {
                    setAudioStreamType(AudioManager.STREAM_ALARM)
                    setDataSource(this@ReminderActivity, Uri.parse(soundUri))
                    isLooping = true
                    prepare()
                    start()
                }

                if (config.increaseVolumeGradually) {
                    scheduleVolumeIncrease(MIN_ALARM_VOLUME_FOR_INCREASING_ALARMS.toFloat(), initialAlarmVolume!!.toFloat(), 0)
                }
            } catch (e: Exception) {
            }
        }
    }

    private fun scheduleVolumeIncrease(lastVolume: Float, maxVolume: Float, delay: Long) {
        increaseVolumeHandler.postDelayed({
            val newLastVolume = (lastVolume + 0.1f).coerceAtMost(maxVolume)
            audioManager?.setStreamVolume(AudioManager.STREAM_ALARM, newLastVolume.toInt(), 0)
            scheduleVolumeIncrease(newLastVolume, maxVolume, INCREASE_VOLUME_DELAY)
        }, delay)
    }

    private fun resetVolumeToInitialValue() {
        initialAlarmVolume?.apply {
            audioManager?.setStreamVolume(AudioManager.STREAM_ALARM, this, 0)
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent?.action == AlarmClock.ACTION_SNOOZE_ALARM) {
            val durationMinutes = intent.getIntExtra(AlarmClock.EXTRA_ALARM_SNOOZE_DURATION, -1)
            if (durationMinutes == -1) {
                snoozeAlarm()
            } else {
                snoozeAlarm(durationMinutes)
            }
        } else {
            finishActivity()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        increaseVolumeHandler.removeCallbacksAndMessages(null)
        maxReminderDurationHandler.removeCallbacksAndMessages(null)
        swipeGuideFadeHandler.removeCallbacksAndMessages(null)
        vibrationHandler.removeCallbacksAndMessages(null)
        if (!finished) {
            finishActivity()
            notificationManager.cancel(ALARM_NOTIF_ID)
        } else {
            destroyEffects()
        }
    }

    private fun destroyEffects() {
        if (config.increaseVolumeGradually) {
            resetVolumeToInitialValue()
        }

        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        vibrator?.cancel()
        vibrator = null
    }

    private fun snoozeAlarm(overrideSnoozeDuration: Int? = null) {
        destroyEffects()
        if (overrideSnoozeDuration != null) {
            setupAlarmClock(alarm!!, overrideSnoozeDuration * MINUTE_SECONDS)
            wasAlarmSnoozed = true
            finishActivity()
        } else if (config.useSameSnooze) {
            setupAlarmClock(alarm!!, config.snoozeTime * MINUTE_SECONDS)
            wasAlarmSnoozed = true
            finishActivity()
        } else {
            showPickSecondsDialog(config.snoozeTime * MINUTE_SECONDS, true, cancelCallback = { finishActivity() }) {
                config.snoozeTime = it / MINUTE_SECONDS
                setupAlarmClock(alarm!!, it)
                wasAlarmSnoozed = true
                finishActivity()
            }
        }
    }

    private fun finishActivity() {
        if (!wasAlarmSnoozed && alarm != null) {
            cancelAlarmClock(alarm!!)
            if (alarm!!.days > 0) {
                scheduleNextAlarm(alarm!!, false)
            }
            if (alarm!!.days < 0) {
                if (alarm!!.oneShot) {
                    alarm!!.isEnabled = false
                    dbHelper.deleteAlarms(arrayListOf(alarm!!))
                } else {
                    dbHelper.updateAlarmEnabledState(alarm!!.id, false)
                }
                updateWidgets()
            }
        }

        finished = true
        destroyEffects()
        finish()
        overridePendingTransition(0, 0)
    }

    private fun showOverLockscreen() {
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )

        if (isOreoMr1Plus()) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        }
    }
}
