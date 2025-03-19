package com.example.todoapp.model

data class Task(
    var id: String = "",
    val userId: String = "",
    val taskName: String = "",
    val startTime: String = "",
    val endTime: String = "",
    var date: String = "",
    val reminderTime: Long = 0L,  // ✅ Change type to Long (Milliseconds)
    var completed: Boolean = false, // ✅ New field
    var imageUrl: String? = null ,// ✅ New field for image URL (nullable)
    val color: Int = 0xFF03DAC5.toInt(), // Default color
    )
