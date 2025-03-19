package com.example.todoapp.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.todoapp.model.Task
import com.example.todoapp.repository.TaskRepository
import com.google.firebase.firestore.FirebaseFirestore
import java.util.UUID

class TaskViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val tasksCollection = db.collection("tasks")
    private val repository = TaskRepository()
    private val _taskList = MutableLiveData<List<Task>>() // LiveData to store tasks
    val taskList: LiveData<List<Task>> = _taskList

    fun loadTasks(date: String) {
        repository.getTasksByDate(date) { tasks ->
            _taskList.postValue(tasks) // ✅ Update LiveData with fetched tasks
        }
    }
    fun updateTaskCompletion(task: Task) {
        if (task.id.isNotEmpty()) { // ✅ Ensure Firestore document ID exists
            tasksCollection.document(task.id)
                .update("completed", task.completed)
                .addOnSuccessListener {
                    Log.d("TaskViewModel", "Task ${task.id} updated successfully")
                }
                .addOnFailureListener { e ->
                    Log.e("TaskViewModel", "Error updating task: ${e.message}")
                }
        } else {
            Log.e("TaskViewModel", "Error: Task ID is empty, cannot update task.")
        }
    }
//    fun scheduleTaskReminder(context: Context, task: Task) {
//        repository.scheduleTaskReminder(context, task)
//    }

}
