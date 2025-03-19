package com.example.todoapp.repository

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import com.example.todoapp.model.Task
import com.example.todoapp.receiver.TaskReminderReceiver
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class TaskRepository {
    private val db = FirebaseFirestore.getInstance()

    fun addTask(task: Task, onSuccess: () -> Unit) {
        db.collection("tasks").document(task.id).set(task)
            .addOnSuccessListener { onSuccess() }
    }

    fun getTasksByDate(date: String, callback: (List<Task>) -> Unit) {
        val userId = "test_user_123"  // ✅ Use the same fixed user ID
        db.collection("tasks")
            .whereEqualTo("userId", userId)
            .whereEqualTo("date", date) // ✅ Fetch tasks for selected date
            .get()
            .addOnSuccessListener { result ->
                val tasks = result.documents.mapNotNull { it.toObject(Task::class.java) }
                callback(tasks) // ✅ Return the fetched tasks
            }
    }
    fun scheduleTaskReminder(context: Context, task: Task) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, TaskReminderReceiver::class.java).apply {
            putExtra("TASK_NAME", task.taskName)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            task.id.hashCode(), // Ensure it's unique
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                Log.e("TaskReminder", "Exact alarm permission NOT granted! Requesting user action.")

                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = Uri.parse("package:${context.packageName}")
                }
                context.startActivity(intent) // Opens Settings for the user to allow exact alarms
                return
            }
        }

    }

}
