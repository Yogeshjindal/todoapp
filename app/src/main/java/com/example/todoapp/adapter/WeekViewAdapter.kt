//package com.example.todoapp.adapter
//
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.TextView
//import androidx.recyclerview.widget.RecyclerView
//import com.example.todoapp.R
//import java.text.SimpleDateFormat
//import java.util.*
//
//class WeekViewAdapter(
//    private var startDate: Calendar,
//    private val onDateSelected: (String) -> Unit
//) : RecyclerView.Adapter<WeekViewAdapter.WeekViewHolder>() {
//
//    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) // Firebase format
//    private val displayFormat = SimpleDateFormat("EEE\ndd", Locale.getDefault()) // Show "Tue\n18"
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeekViewHolder {
//        val view = LayoutInflater.from(parent.context)
//            .inflate(R.layout.item_week_day, parent, false)
//        return WeekViewHolder(view)
//    }
//
//    override fun onBindViewHolder(holder: WeekViewHolder, position: Int) {
//        val calendar = startDate.clone() as Calendar
//        calendar.add(Calendar.DAY_OF_MONTH, position - 3) // Show 3 past, current, and 3 future
//
//        val fullDate = dateFormat.format(calendar.time)
//        holder.txtDay.text = displayFormat.format(calendar.time)
//
//        // Highlight today
//        val today = dateFormat.format(Date())
//        holder.txtDay.setBackgroundResource(
//            if (fullDate == today) R.drawable.selected_date_background
//            else R.drawable.default_date_background
//        )
//
//        holder.itemView.setOnClickListener {
//            onDateSelected(fullDate)
//            notifyDataSetChanged() // Refresh selection
//        }
//    }
//
//    override fun getItemCount(): Int = 7 // Show 7 days at a time
//
//    class WeekViewHolder(view: View) : RecyclerView.ViewHolder(view) {
//        val txtDay: TextView = view.findViewById(R.id.tvDay)
//    }
//}
