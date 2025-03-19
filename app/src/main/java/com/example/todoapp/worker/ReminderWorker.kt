package com.example.todoapp.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.todoapp.R

class ReminderWorker(context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    override fun doWork(): Result {
        val taskName = inputData.getString("TASK_NAME") ?: "Task Reminder"
        showNotificationWithSound(taskName)
        vibrateDevice()
        playAlarmSound()
        return Result.success()
    }

    private fun showNotificationWithSound(taskName: String) {
        val channelId = "task_reminder_channel"
        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Task Reminder",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                enableVibration(true)
                vibrationPattern = longArrayOf(500, 1000, 500, 1000) // Vibrate pattern
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setContentTitle("Reminder Alert 🚀")
            .setContentText("It's time for: $taskName")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    private fun vibrateDevice() {
        val vibrator = applicationContext.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val vibrationEffect = VibrationEffect.createWaveform(
                longArrayOf(500, 1000, 500, 1000), // Pattern: Vibrate-Pause-Vibrate
                -1 // No repeat
            )
            vibrator.vibrate(vibrationEffect)
        } else {
            vibrator.vibrate(longArrayOf(500, 1000, 500, 1000), -1) // For older devices
        }
    }

    private fun playAlarmSound() {
        try {
            val alarmSound: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            val ringtone: Ringtone = RingtoneManager.getRingtone(applicationContext, alarmSound)
            ringtone.play()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
