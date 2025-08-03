package com.mobicom.mco.pokus.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.CountDownTimer
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.lifecycle.MutableLiveData
import com.mobicom.mco.pokus.MainActivity
import com.mobicom.mco.pokus.R

class TimerService : Service() {

    private val binder = TimerBinder()
    private var countDownTimer: CountDownTimer? = null
    var timeLeftInMillis: Long = POMODORO_DURATION
    var isTimerRunning: Boolean = false

    val timeLeftLiveData = MutableLiveData<Long>()
    val isFinishedLiveData = MutableLiveData<Boolean>()

    companion object {
        const val POMODORO_DURATION = 25 * 60 * 1000L
        const val SHORT_BREAK_DURATION = 5 * 60 * 1000L
        const val LONG_BREAK_DURATION = 15 * 60 * 1000L
        const val NOTIFICATION_ID = 1
        const val CHANNEL_ID = "TimerChannel"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    inner class TimerBinder : Binder() {
        fun getService(): TimerService = this@TimerService
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    fun startTimer() {
        if (!isTimerRunning) {
            isTimerRunning = true
            isFinishedLiveData.postValue(false)
            startForeground(NOTIFICATION_ID, createNotification("Timer is running..."))

            countDownTimer = object : CountDownTimer(timeLeftInMillis, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    timeLeftInMillis = millisUntilFinished
                    timeLeftLiveData.postValue(timeLeftInMillis)
                    // Update notification with current time
                    updateNotification(formatTime(timeLeftInMillis))
                }

                override fun onFinish() {
                    isTimerRunning = false
                    isFinishedLiveData.postValue(true)
                    stopForeground(true) // Remove notification when finished
                    resetTimer(POMODORO_DURATION)
                }
            }.start()
        }
    }

    fun pauseTimer() {
        countDownTimer?.cancel()
        isTimerRunning = false
        stopForeground(false) // Make notification dismissible
    }

    fun resetTimer(duration: Long) {
        pauseTimer()
        timeLeftInMillis = duration
        timeLeftLiveData.postValue(timeLeftInMillis)
        isFinishedLiveData.postValue(false)
        updateNotification(formatTime(duration))
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Timer Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    private fun createNotification(text: String) = NotificationCompat.Builder(this, CHANNEL_ID)
        .setContentTitle("Pokus Study Session")
        .setContentText(text)
        .setSmallIcon(R.drawable.sessions) // Replace with your own icon
        .setContentIntent(PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE))
        .build()

    private fun updateNotification(text: String) {
        val notification = createNotification(text)
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, notification)
    }

    // Helper to format time for notification
    private fun formatTime(millis: Long): String {
        val minutes = java.util.concurrent.TimeUnit.MILLISECONDS.toMinutes(millis)
        val seconds = java.util.concurrent.TimeUnit.MILLISECONDS.toSeconds(millis) - java.util.concurrent.TimeUnit.MINUTES.toSeconds(minutes)
        return String.format("%02d:%02d", minutes, seconds)
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel() // Clean up timer
    }
}