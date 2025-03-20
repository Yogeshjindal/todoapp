package com.example.todoapp.adapter

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.todoapp.R
import com.example.todoapp.databinding.ItemTaskBinding
import com.example.todoapp.model.Task
import java.util.Collections

class TaskAdapter(private val tasks: MutableList<Task>, private val onTaskChecked: (Task) -> Unit,
                  private val onTaskClicked: (Task) -> Unit // New click listener for editing
) :
    RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    inner class TaskViewHolder(val binding: ItemTaskBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(task: Task) {
            binding.txtTaskName.text = task.taskName
            binding.txtTime.text = "${task.startTime} - ${task.endTime}"
            binding.chkCompleted.isChecked = task.completed
            itemView.setOnClickListener {
                onTaskClicked(task) // Handle task click
            }
            // ✅ Apply or remove strikethrough effect
            applyStrikeThrough(binding, task.completed)

            // ✅ Prevent unwanted triggers when checkbox is recycled
            binding.chkCompleted.setOnCheckedChangeListener(null)
            binding.chkCompleted.setOnCheckedChangeListener { _, isChecked ->
                task.completed = isChecked
                applyStrikeThrough(binding, isChecked) // ✅ Update UI
                onTaskChecked(task) // ✅ Notify MainActivity to update the database
            }

            // ✅ Load image with Glide (fallback if null)
            Glide.with(binding.root.context)
                .load(task.imageUrl ?: R.drawable.smile) // ✅ Default image
                .placeholder(R.drawable.smile) // ✅ Show smile image while loading
                .error(R.drawable.smile) // ✅ Show smile image if loading fails
                .override(80, 80) // ✅ Resize for consistency
                .centerCrop()
                .into(binding.imgTask) // ✅ Bind to ImageView
        }

        // ✅ Function to apply or remove strikethrough
        private fun applyStrikeThrough(binding: ItemTaskBinding, isCompleted: Boolean) {
            if (isCompleted) {
                binding.txtTaskName.paintFlags = binding.txtTaskName.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            } else {
                binding.txtTaskName.paintFlags = binding.txtTaskName.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TaskViewHolder(binding)
    }

    override fun getItemCount() = tasks.size

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]  // ✅ Get task at position
        holder.bind(task)
        holder.binding.cardViewTask.setCardBackgroundColor(task.color) // ✅ Apply selected color
    }


    // ✅ Function to swap items
    fun moveItem(fromPosition: Int, toPosition: Int) {
        Collections.swap(tasks, fromPosition, toPosition)
        notifyItemMoved(fromPosition, toPosition)
    }

    // ✅ Update tasks when new data arrives
    fun updateTasks(newTasks: List<Task>) {
        tasks.clear()
        tasks.addAll(newTasks)
        notifyDataSetChanged()
    }
}
