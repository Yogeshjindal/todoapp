package com.example.todoapp

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.media.audiofx.BassBoost
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.todoapp.adapter.TaskAdapter
import com.example.todoapp.databinding.ActivityMainBinding
import com.example.todoapp.utils.TaskCarryoverHelper
import com.example.todoapp.viewmodel.TaskViewModel
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel: TaskViewModel by viewModels()
    private lateinit var taskAdapter: TaskAdapter
    private var selectedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
        }

        setupRecyclerView()
        setupWeekView()
        loadTasks(selectedDate)

        viewModel.taskList.observe(this) { tasks ->
            Log.d("MainActivity", "Tasks Loaded: ${tasks.size}") // Debugging log
            taskAdapter.updateTasks(tasks) // ✅ Update RecyclerView dynamically
        }

        binding.fabAddTask.setOnClickListener {
            val intent = Intent(this, AddTaskActivity::class.java)
            intent.putExtra("SELECTED_DATE", selectedDate)
            startActivity(intent)
        }

        viewModel.taskList.observe(this) { tasks ->
            taskAdapter.updateTasks(tasks)
// ✅ Show duck image & text if there are no tasks
            if (tasks.isEmpty()) {
                binding.imgNoTasks.visibility = View.VISIBLE
                binding.txtNoTasks.visibility = View.VISIBLE
            } else {
                binding.imgNoTasks.visibility = View.GONE
                binding.txtNoTasks.visibility = View.GONE
            }

        }
    }

    private fun setupRecyclerView() {
        taskAdapter = TaskAdapter(mutableListOf()) { updatedTask ->
            viewModel.updateTaskCompletion(updatedTask) // ✅ Save changes to Firestore
        }
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = taskAdapter

        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val fromPosition = viewHolder.adapterPosition
                val toPosition = target.adapterPosition
                taskAdapter.moveItem(fromPosition, toPosition) // ✅ Swap positions
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                // No swipe action needed
            }
        })

        itemTouchHelper.attachToRecyclerView(binding.recyclerView)
    }

    private fun loadTasks(date: String) {
        Log.d("MainActivity", "Loading tasks for date: $date") // Debugging log
        viewModel.loadTasks(date)
    }
    private fun setupWeekView() {
        val dates = listOf(
            binding.txtDay1, binding.txtDay2, binding.txtDay3, binding.txtDay4,
            binding.txtDay5, binding.txtDay6, binding.txtDay7
        )

        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) // Firebase format
        val displayFormat = SimpleDateFormat("EEE\ndd", Locale.getDefault()) // Show "Tue\n18"
        val selectedDisplayFormat = SimpleDateFormat("EEEE, MMM dd", Locale.getDefault()) // Full format for middle TextView

        val today = dateFormat.format(Date()) // Get today's date

        val weekDates = mutableListOf<String>()

        // Store actual dates to prevent misalignment
        for (i in dates.indices) {
            val fullDate = dateFormat.format(calendar.time)
            weekDates.add(fullDate) // Store dates in order
            dates[i].text = displayFormat.format(calendar.time)

            if (fullDate == today) {
                selectedDate = fullDate
                binding.txtSelectedDate.text = selectedDisplayFormat.format(calendar.time) // ✅ Show today's date initially
                dates[i].setBackgroundResource(R.drawable.selected_date_background)
            }

            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        // Add click listeners correctly
        for (i in dates.indices) {
            dates[i].setOnClickListener {
                selectedDate = weekDates[i] // Correctly use stored date
                loadTasks(selectedDate)

                val selectedCalendar = Calendar.getInstance()
                selectedCalendar.time = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(selectedDate)!!

                binding.txtSelectedDate.text = selectedDisplayFormat.format(selectedCalendar.time) // ✅ Fix middle date issue

                // Reset all backgrounds & highlight the selected one
                dates.forEach { it.setBackgroundResource(R.drawable.default_date_background) }
                dates[i].setBackgroundResource(R.drawable.selected_date_background)
            }
        }

        loadTasks(selectedDate)
    }

    override fun onResume() {
        super.onResume()
       // TaskCarryoverHelper.carryOverTasks(this, viewModel) // ✅ Call the helper function
        loadTasks(selectedDate) // ✅ Reload tasks every time the activity is resumed
    }
}
