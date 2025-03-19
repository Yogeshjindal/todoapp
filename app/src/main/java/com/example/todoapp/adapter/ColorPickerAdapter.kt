package com.example.todoapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.todoapp.R

class ColorPickerAdapter(
    private val colors: List<Int>,
    private val onColorSelected: (Int) -> Unit
) : RecyclerView.Adapter<ColorPickerAdapter.ColorViewHolder>() {

    private var selectedColor: Int? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColorViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_color_circle, parent, false)
        return ColorViewHolder(view)
    }

    override fun onBindViewHolder(holder: ColorViewHolder, position: Int) {
        val color = colors[position]
        holder.bind(color, color == selectedColor)

        holder.itemView.setOnClickListener {
            selectedColor = color
            onColorSelected(color)
            notifyDataSetChanged()
        }
    }

    override fun getItemCount(): Int = colors.size

    class ColorViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val colorView: ImageView = view.findViewById(R.id.imgColorCircle)

        fun bind(color: Int, isSelected: Boolean) {
            colorView.setBackgroundColor(color)  // ✅ Corrected color setting

            // Highlight the selected color
            colorView.alpha = if (isSelected) 1.0f else 0.5f
        }
    }
}
