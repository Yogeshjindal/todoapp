package com.example.todoapp.utils

import android.content.Context
import android.util.Log
import com.example.todoapp.viewmodel.TaskViewModel
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

object TaskCarryoverHelper {

    fun carryOverTasks(context: Context, viewModel: TaskViewModel) {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_MONTH, 1) // Move to next day
        val nextDay = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)

        val sharedPrefs = context.getSharedPreferences("TaskPrefs", Context.MODE_PRIVATE)
        val lastUpdatedDay = sharedPrefs.getString("LAST_UPDATED_DAY", "")

        if (lastUpdatedDay != today) {
            val db = FirebaseFirestore.getInstance()
            val tasksRef = db.collection("tasks")

            tasksRef.whereEqualTo("date", today)
                .whereEqualTo("completed", false) // Unchecked tasks only
                .get()
                .addOnSuccessListener { result ->
                    for (document in result) {
                        val task = document.toObject(com.example.todoapp.model.Task::class.java)
                        task.id = UUID.randomUUID().toString() // Generate new ID
                        task.date = nextDay // Move to next day

                        // Add to Firestore as a new task
                        tasksRef.document(task.id).set(task)
                    }
                    // Update SharedPreferences to prevent duplicate carryovers
                    sharedPrefs.edit().putString("LAST_UPDATED_DAY", today).apply()
                }
                .addOnFailureListener { e ->
                    Log.e("TaskCarryover", "Error carrying over tasks: ${e.message}")
                }
        }
    }
}
