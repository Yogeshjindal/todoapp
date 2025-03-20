package com.example.todoapp

import android.app.AlarmManager
import android.app.TimePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
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
import com.example.todoapp.viewmodel.TaskViewModel
import com.example.todoapp.worker.ReminderWorker
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class AddTaskActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddTaskBinding
    private val repository = TaskRepository()
    private val viewModel: TaskViewModel by viewModels()
    private var taskId: String? = null
    private var selectedDate: String = getCurrentDate()
    private var imageUrl: String? = null
    private var selectedColor: Int = android.graphics.Color.RED

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initializeUI()
    }

    private fun initializeUI() {
        retrieveIntentData()
        setupColorPicker()
        setupListeners()
        checkAlarmPermission()
    }

    private fun retrieveIntentData() {
        taskId = intent.getStringExtra("TASK_ID")
        binding.etTaskName.setText(intent.getStringExtra("TASK_NAME"))
        binding.etStartTime.setText(intent.getStringExtra("TASK_START_TIME"))
        binding.etEndTime.setText(intent.getStringExtra("TASK_END_TIME"))
        selectedDate = intent.getStringExtra("SELECTED_DATE") ?: getCurrentDate()

        if (taskId != null) {
            binding.btnSave.text = "Update Task"
            binding.btnDeleteTask.visibility = View.VISIBLE
        } else {
            binding.btnDeleteTask.visibility = View.GONE
        }
    }

    private fun setupListeners() {
        binding.ivClose.setOnClickListener { finish() }
        binding.btnSave.setOnClickListener { saveOrUpdateTask() }
        binding.btnDeleteTask.setOnClickListener { deleteTask() }
        binding.etStartTime.setOnClickListener { pickTime(binding.etStartTime) }
        binding.etEndTime.setOnClickListener { pickTime(binding.etEndTime) }
        binding.etReminderTime.setOnClickListener { pickTime(binding.etReminderTime) }

        binding.etTaskName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!s.isNullOrEmpty()) fetchTaskImage(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun checkAlarmPermission() {
        val alarmManager = getSystemService(AlarmManager::class.java)
        if (!alarmManager.canScheduleExactAlarms()) {
            Toast.makeText(this, "Exact alarm permission is not granted!", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupColorPicker() {
        val colors = listOf(
            android.graphics.Color.RED, android.graphics.Color.BLUE,
            android.graphics.Color.GREEN, android.graphics.Color.YELLOW,
            android.graphics.Color.CYAN, android.graphics.Color.MAGENTA,
            android.graphics.Color.BLACK, android.graphics.Color.LTGRAY,
            android.graphics.Color.DKGRAY, android.graphics.Color.WHITE,
            android.graphics.Color.rgb(255, 165, 0),  // Orange
            android.graphics.Color.rgb(128, 0, 128),  // Purple
            android.graphics.Color.rgb(255, 192, 203), // Pink
            android.graphics.Color.rgb(139, 69, 19),  // Brown
            android.graphics.Color.rgb(0, 255, 127)   // Spring Green
        )

        binding.recyclerViewColors.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = ColorPickerAdapter(colors) { color ->
                selectedColor = color
                binding.cardTaskDetails.setCardBackgroundColor(color)
            }
        }

        LinearSnapHelper().attachToRecyclerView(binding.recyclerViewColors)
    }

    private fun saveOrUpdateTask() {
        val userId = "test_user_123"
        val reminderTimeMillis = TimeUtils.parseReminderTime(selectedDate, binding.etReminderTime.text.toString())

        val task = Task(
            id = taskId ?: UUID.randomUUID().toString(),
            userId = userId,
            taskName = binding.etTaskName.text.toString(),
            startTime = binding.etStartTime.text.toString(),
            endTime = binding.etEndTime.text.toString(),
            date = selectedDate,
            reminderTime = reminderTimeMillis,
            imageUrl = imageUrl,
            color = selectedColor
        )

        if (taskId != null) {
            viewModel.updateTask(task)
            Toast.makeText(this, "Task updated successfully", Toast.LENGTH_SHORT).show()
        } else {
            repository.addTask(task) {
                scheduleReminder(task.taskName, reminderTimeMillis)
            }
        }
        finish()
    }

    private fun deleteTask() {
        taskId?.let {
            viewModel.deleteTask(it)
            Toast.makeText(this, "Task deleted successfully", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun scheduleReminder(taskName: String, reminderTimeMillis: Long) {
        val delay = reminderTimeMillis - System.currentTimeMillis()
        if (delay > 0) {
            val workRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setInputData(workDataOf("TASK_NAME" to taskName))
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
                    imageUrl = url
                    Glide.with(this)
                        .load(url)
                        .override(300, 200)
                        .centerCrop()
                        .into(binding.ivTaskImage)
                }
            },
            onError = {
                runOnUiThread {
                    Glide.with(this)
                        .load(R.drawable.smile)
                        .override(300, 200)
                        .centerCrop()
                        .into(binding.ivTaskImage)
                }
            })
    }

    private fun getCurrentDate(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }
}
