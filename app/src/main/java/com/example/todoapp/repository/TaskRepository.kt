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


}
