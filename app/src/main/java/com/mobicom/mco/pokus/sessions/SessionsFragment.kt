package com.mobicom.mco.pokus.sessions

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.mobicom.mco.pokus.R
import com.mobicom.mco.pokus.databinding.ActivitySessionsBinding
import com.mobicom.mco.pokus.services.TimerService
import java.util.concurrent.TimeUnit

class SessionsFragment : Fragment() {

    private var _binding: ActivitySessionsBinding? = null
    private val binding get() = _binding!!

    private var timerService: TimerService? = null
    private var isBound = false
    private lateinit var serviceIntent: Intent

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as TimerService.TimerBinder
            timerService = binder.getService()
            isBound = true
            updateUI() // Update UI as soon as we connect
            setupObservers()
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            isBound = false
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ActivitySessionsBinding.inflate(inflater, container, false)
        serviceIntent = Intent(requireContext(), TimerService::class.java)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
    }

    override fun onStart() {
        super.onStart()
        requireActivity().bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE)
    }

    override fun onStop() {
        super.onStop()
        if (isBound) {
            requireActivity().unbindService(connection)
            isBound = false
        }
    }

    private fun updateUI() {
        timerService?.let {
            binding.timerText.text = formatTime(it.timeLeftInMillis)
            binding.startBtn.text = if (it.isTimerRunning) "Pause" else "Start"
        }
    }

    private fun setupClickListeners() {
        binding.startBtn.setOnClickListener {
            if (timerService?.isTimerRunning == true) {
                timerService?.pauseTimer()
            } else {
                requireActivity().startService(serviceIntent) // Start the service to make it long-running
                timerService?.startTimer()
            }
            updateUI()
        }

        binding.toggleGroup.addOnButtonCheckedListener { group, checkedId, isChecked ->
            if (isChecked && isBound) {
                val duration = when (checkedId) {
                    R.id.shortBreakBtn -> TimerService.SHORT_BREAK_DURATION
                    R.id.longBreakBtn -> TimerService.LONG_BREAK_DURATION
                    else -> TimerService.POMODORO_DURATION
                }
                timerService?.resetTimer(duration)
                updateUI()
            }
        }

        binding.finishBtn.setOnClickListener {
            if(isBound) {
                timerService?.pauseTimer()
                requireActivity().stopService(serviceIntent)
            }
            // TODO: Navigate to SaveSessionActivity
        }
    }

    private fun setupObservers() {
        timerService?.timeLeftLiveData?.observe(viewLifecycleOwner) { millisLeft ->
            binding.timerText.text = formatTime(millisLeft)
        }

        timerService?.isFinishedLiveData?.observe(viewLifecycleOwner) { isFinished ->
            if (isFinished) {
                updateUI()
                // TODO: Play a sound
            }
        }
    }

    private fun formatTime(millis: Long): String {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(minutes)
        return String.format("%02d:%02d", minutes, seconds)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}