package com.mobicom.mco.pokus.sessions

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mobicom.mco.pokus.R
import com.mobicom.mco.pokus.data.repository.PokusRepository
import com.mobicom.mco.pokus.todo.TodoItem

class SessionTaskAdapter(
    private val items: MutableList<TodoItem>,
    private val context: Context // Added context to get repository instance
) : RecyclerView.Adapter<SessionTaskAdapter.SessionTaskViewHolder>() {

    private var isTimerRunning = false
    // Get an instance of the repository to interact with the database
    private val repository = PokusRepository.getInstance(context)

    // Method for the fragment to tell the adapter the timer's state
    fun setTimerRunning(isRunning: Boolean) {
        isTimerRunning = isRunning
        notifyDataSetChanged() // Redraw the list to show/hide buttons
    }

    inner class SessionTaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val checkbox: CheckBox = itemView.findViewById(R.id.checkbox)
        val title: TextView = itemView.findViewById(R.id.todoTitle)
        val removeButton: ImageButton = itemView.findViewById(R.id.btn_remove_task)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SessionTaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.itemtodo, parent, false)
        return SessionTaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: SessionTaskViewHolder, position: Int) {
        val item = items[position]
        holder.title.text = item.title
        holder.checkbox.isChecked = item.isChecked

        holder.removeButton.visibility = if (isTimerRunning) View.GONE else View.VISIBLE

        holder.checkbox.setOnCheckedChangeListener { _, isChecked ->
            item.isChecked = isChecked
        }

        holder.removeButton.setOnClickListener {
            val currentPosition = holder.adapterPosition
            if (currentPosition != RecyclerView.NO_POSITION) {
                // Get the item to remove
                val itemToRemove = items[currentPosition]
                // **NEW**: Delete the task from the database
                repository.deleteTask(itemToRemove.id)
                // Remove the item from this adapter's list
                items.removeAt(currentPosition)
                notifyItemRemoved(currentPosition)
            }
        }
    }

    override fun getItemCount(): Int = items.size

    fun addItem(item: TodoItem) {
        items.add(item)
        notifyItemInserted(items.size - 1)
    }
}