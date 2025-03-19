package com.example.todoapp

import android.app.AlarmManager
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.bumptech.glide.Glide
import com.example.todoapp.adapter.ColorPickerAdapter
import com.example.todoapp.databinding.ActivityAddTaskBinding
import com.example.todoapp.model.Task
import com.example.todoapp.repository.TaskRepository
import com.example.todoapp.utils.ImageHelper
import com.example.todoapp.utils.TimeUtils
import com.example.todoapp.worker.ReminderWorker
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class AddTaskActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddTaskBinding
    private val repository = TaskRepository()
    private lateinit var selectedDate: String
    private var imageUrl: String? = null // ✅ Store image URL globally
    private var selectedColor: Int = Color.RED // Default color

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupColorPicker()
        binding.ivClose.setOnClickListener {
            finish() // Closes the current activity and goes back
        }
        if (!getSystemService(AlarmManager::class.java).canScheduleExactAlarms()) {
            Toast.makeText(this, "Exact alarm permission is not granted!", Toast.LENGTH_LONG).show()
        }

        selectedDate = intent.getStringExtra("SELECTED_DATE")
            ?: SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        // Click Listeners
        binding.btnSave.setOnClickListener { saveTask() }
        binding.etStartTime.setOnClickListener { pickTime(binding.etStartTime) }
        binding.etEndTime.setOnClickListener { pickTime(binding.etEndTime) }
        binding.etReminderTime.setOnClickListener { pickTime(binding.etReminderTime) }

        // Fetch image dynamically when task name changes
        binding.etTaskName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val taskName = s.toString().trim()
                if (taskName.isNotEmpty()) {
                    fetchTaskImage(taskName)
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupColorPicker() {
        val colors = listOf(
            Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW, Color.CYAN,
            Color.MAGENTA, Color.BLACK, Color.LTGRAY, Color.DKGRAY,
            Color.WHITE, Color.rgb(255, 165, 0),  // Orange
            Color.rgb(128, 0, 128),  // Purple
            Color.rgb(255, 192, 203), // Pink
            Color.rgb(139, 69, 19),  // Brown
            Color.rgb(0, 255, 127)   // Spring Green
        )

        val colorAdapter = ColorPickerAdapter(colors) { color ->
            selectedColor = color // ✅ Store selected color globally
            binding.cardTaskDetails.setCardBackgroundColor(color) // ✅ Changes CardView color
        }

        binding.recyclerViewColors.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = colorAdapter
        }

        val snapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(binding.recyclerViewColors)
    }

    private fun saveTask() {
        val userId ="test_user_123"
        val reminderTimeMillis = TimeUtils.parseReminderTime(selectedDate, binding.etReminderTime.text.toString())

        val task = Task(
            id = UUID.randomUUID().toString(),
            userId = userId,
            taskName = binding.etTaskName.text.toString(),
            startTime = binding.etStartTime.text.toString(),
            endTime = binding.etEndTime.text.toString(),
            date = selectedDate,
            reminderTime = reminderTimeMillis,
            imageUrl = imageUrl,
            color = selectedColor
        )

        repository.addTask(task) {
            scheduleReminder(task.taskName, reminderTimeMillis) // Schedule WorkManager task reminder
            finish()
        }
    }

    private fun scheduleReminder(taskName: String, reminderTimeMillis: Long) {
        val delay = reminderTimeMillis - System.currentTimeMillis()

        if (delay > 0) {
            val workRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setInputData(
                    workDataOf("TASK_NAME" to taskName)
                )
                .build()

            WorkManager.getInstance(this).enqueue(workRequest)
            Toast.makeText(this, "Reminder set for $taskName", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Reminder time is in the past!", Toast.LENGTH_SHORT).show()
        }
    }


    private fun pickTime(textView: TextView) {
        val calendar = Calendar.getInstance()
        TimePickerDialog(this, { _, hour, minute ->
            textView.text = String.format("%02d:%02d", hour, minute)
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
    }

    private fun fetchTaskImage(taskName: String) {
        ImageHelper.fetchImage(taskName,
            onSuccess = { url ->
                runOnUiThread {
                    imageUrl = url // ✅ Store the image URL globally
                    Glide.with(this).load(url)
                        .override(300, 200) // Set a better resolution
                        .centerCrop() // Crop to fill the ImageView properly
                        .into(binding.ivTaskImage)
                }
            },
            onError = {
                runOnUiThread {
                    Glide.with(this)
                        .load(R.drawable.smile) // ✅ Load smile image from drawable
                        .override(300, 200)
                        .centerCrop()
                        .into(binding.ivTaskImage)
                }
            })
    }
}
