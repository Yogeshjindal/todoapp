package com.example.todoapp.utils

import java.text.SimpleDateFormat
import java.util.*

object TimeUtils {
    fun parseReminderTime(selectedDate: String, reminderTime: String): Long {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            val date = sdf.parse("$selectedDate $reminderTime") // Convert to Date
            date?.time ?: 0L // Convert Date to Milliseconds
        } catch (e: Exception) {
            e.printStackTrace()
            0L // Default to 0 if parsing fails
        }
    }
}
