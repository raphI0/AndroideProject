package com.simplemobiletools.clock.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.simplemobiletools.clock.activities.SimpleActivity
import com.simplemobiletools.clock.adapters.TimerAdapter
import com.simplemobiletools.clock.databinding.FragmentTimerBinding
import com.simplemobiletools.clock.dialogs.EditTimerDialog
import com.simplemobiletools.clock.extensions.config
import com.simplemobiletools.clock.extensions.createNewTimer
import com.simplemobiletools.clock.extensions.timerHelper
import com.simplemobiletools.clock.helpers.DisabledItemChangeAnimator
import com.simplemobiletools.clock.models.Timer
import com.simplemobiletools.clock.models.TimerEvent
import com.simplemobiletools.commons.extensions.getProperBackgroundColor
import com.simplemobiletools.commons.extensions.getProperTextColor
import com.simplemobiletools.commons.extensions.hideKeyboard
import com.simplemobiletools.commons.extensions.updateTextColors
import com.simplemobiletools.commons.models.AlarmSound
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class TimerFragment : Fragment() {
    private val INVALID_POSITION = -1
    private lateinit var binding: FragmentTimerBinding
    private lateinit var timerAdapter: TimerAdapter
    private var timerPositionToScrollTo = INVALID_POSITION
    private var currentEditAlarmDialog: EditTimerDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EventBus.getDefault().register(this)
    }

    override fun onDestroy() {
        EventBus.getDefault().unregister(this)
        super.onDestroy()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentTimerBinding.inflate(inflater, container, false).apply {
            timersList.itemAnimator = DisabledItemChangeAnimator()
            timerAdd.setOnClickListener {
                activity?.run {
                    hideKeyboard()
                    openEditTimer(createNewTimer())
                }
            }
        }

        initOrUpdateAdapter()
        refreshTimers()

        // the initial timer is created asynchronously at first launch, make sure we show it once created
        if (context?.config?.appRunCount == 1) {
            Handler(Looper.getMainLooper()).postDelayed({
                refreshTimers()
            }, 1000)
        }

        return binding.root
    }

    private fun initOrUpdateAdapter() {
        if (this::timerAdapter.isInitialized) {
            timerAdapter.updatePrimaryColor()
            timerAdapter.updateBackgroundColor(requireContext().getProperBackgroundColor())
            timerAdapter.updateTextColor(requireContext().getProperTextColor())
        } else {
            timerAdapter = TimerAdapter(requireActivity() as SimpleActivity, binding.timersList, ::refreshTimers, ::openEditTimer)
            binding.timersList.adapter = timerAdapter
        }
    }

    override fun onResume() {
        super.onResume()
        requireContext().updateTextColors(binding.root)
        initOrUpdateAdapter()
        refreshTimers()
    }

    private fun refreshTimers(scrollToLatest: Boolean = false) {
        activity?.timerHelper?.getTimers { timers ->
            activity?.runOnUiThread {
                timerAdapter.submitList(timers) {
                    getView()?.post {
                        if (timerPositionToScrollTo != INVALID_POSITION && timerAdapter.itemCount > timerPositionToScrollTo) {
                            binding.timersList.scrollToPosition(timerPositionToScrollTo)
                            timerPositionToScrollTo = INVALID_POSITION
                        } else if (scrollToLatest) {
                            binding.timersList.scrollToPosition(timers.lastIndex)
                        }
                    }
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: TimerEvent.Refresh) {
        refreshTimers()
    }

    fun updateAlarmSound(alarmSound: AlarmSound) {
        currentEditAlarmDialog?.updateAlarmSound(alarmSound)
    }

    fun updatePosition(timerId: Int) {
        activity?.timerHelper?.getTimers { timers ->
            val position = timers.indexOfFirst { it.id == timerId }
            if (position != INVALID_POSITION) {
                activity?.runOnUiThread {
                    if (timerAdapter.itemCount > position) {
                        binding.timersList.scrollToPosition(position)
                    } else {
                        timerPositionToScrollTo = position
                    }
                }
            }
        }
    }

    private fun openEditTimer(timer: Timer) {
        currentEditAlarmDialog = EditTimerDialog(activity as SimpleActivity, timer) {
            currentEditAlarmDialog = null
            refreshTimers()
        }
    }
}
