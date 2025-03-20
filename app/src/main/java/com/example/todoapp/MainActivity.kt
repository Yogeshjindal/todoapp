package com.example.todoapp

import android.Manifest
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.todoapp.adapter.TaskAdapter
import com.example.todoapp.databinding.ActivityMainBinding
import com.example.todoapp.model.Task
import com.example.todoapp.viewmodel.TaskViewModel
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel: TaskViewModel by viewModels()
    private lateinit var taskAdapter: TaskAdapter
    private val calendar = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val displayFormat = SimpleDateFormat("EEE\ndd", Locale.getDefault())
    private val selectedDisplayFormat = SimpleDateFormat("EEEE, MMM dd", Locale.getDefault())
    private var selectedDate = dateFormat.format(calendar.time)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        requestNotificationPermission()
        setupRecyclerView()
        setupListeners()
        updateWeekView()
        observeTasks()
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
        }
    }

    private fun setupRecyclerView() {
        taskAdapter = TaskAdapter(mutableListOf(),
            { updatedTask -> viewModel.updateTaskCompletion(updatedTask) },
            { task -> openTaskEditor(task) }
        )
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = taskAdapter
        }
    }

    private fun setupListeners() {
        binding.fabAddTask.setOnClickListener { openTaskEditor() }
        binding.txtSelectedDate.setOnClickListener { showDatePicker() }
        binding.btnBack.setOnClickListener { shiftWeek(-7) }
        binding.btnNext.setOnClickListener { shiftWeek(7) }
    }

    private fun observeTasks() {
        viewModel.taskList.observe(this) { tasks ->
            taskAdapter.updateTasks(tasks)
            toggleNoTasksView(tasks.isEmpty())
        }
        loadTasks(selectedDate)
    }

    private fun loadTasks(date: String) {
        Log.d("MainActivity", "Loading tasks for date: $date")
        viewModel.loadTasks(date)
    }

    private fun updateWeekView() {
        val dates = listOf(
            binding.txtDay1, binding.txtDay2, binding.txtDay3, binding.txtDay4,
            binding.txtDay5, binding.txtDay6, binding.txtDay7
        )
        val weekDates = mutableListOf<String>()

        dates.forEachIndexed { index, textView ->
            val fullDate = dateFormat.format(calendar.time)
            weekDates.add(fullDate)
            textView.text = displayFormat.format(calendar.time)
            textView.setBackgroundResource(if (fullDate == selectedDate) R.drawable.selected_date_background else R.drawable.default_date_background)
            textView.setOnClickListener { selectDate(fullDate, weekDates, dates) }
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        calendar.time = dateFormat.parse(weekDates[0])!!
        binding.txtSelectedDate.text = selectedDisplayFormat.format(dateFormat.parse(selectedDate)!!)
    }

    private fun selectDate(date: String, weekDates: List<String>, dateViews: List<View>) {
        selectedDate = date
        loadTasks(selectedDate)
        binding.txtSelectedDate.text = selectedDisplayFormat.format(dateFormat.parse(selectedDate)!!)
        dateViews.forEach { it.setBackgroundResource(R.drawable.default_date_background) }
        dateViews[weekDates.indexOf(date)].setBackgroundResource(R.drawable.selected_date_background)
    }

    private fun shiftWeek(days: Int) {
        calendar.add(Calendar.DAY_OF_MONTH, days)
        updateWeekView()
    }

    private fun showDatePicker() {

        DatePickerDialog(this, { _, year, month, dayOfMonth ->
            calendar.set(year, month, dayOfMonth)
            selectedDate = dateFormat.format(calendar.time)
            loadTasks(selectedDate)
            updateWeekView()
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun openTaskEditor(task: Task? = null) {
        val intent = Intent(this, AddTaskActivity::class.java).apply {
            task?.let {
                putExtra("TASK_ID", it.id)
                putExtra("TASK_NAME", it.taskName)
                putExtra("TASK_DATE", it.date)
                putExtra("TASK_START_TIME", it.startTime)
                putExtra("TASK_END_TIME", it.endTime)
            } ?: putExtra("SELECTED_DATE", selectedDate)
        }
        startActivity(intent)
    }

    private fun toggleNoTasksView(isEmpty: Boolean) {
        binding.imgNoTasks.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.txtNoTasks.visibility = if (isEmpty) View.VISIBLE else View.GONE
    }

    override fun onResume() {
        super.onResume()
        loadTasks(selectedDate)
    }
}
