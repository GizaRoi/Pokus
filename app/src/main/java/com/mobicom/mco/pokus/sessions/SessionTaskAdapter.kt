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
    private val context: Context // We already have context
) : RecyclerView.Adapter<SessionTaskAdapter.SessionTaskViewHolder>() {

    private var isTimerRunning = false
    private val repository = PokusRepository.getInstance(context)

    fun setTimerRunning(isRunning: Boolean) {
        isTimerRunning = isRunning
        notifyDataSetChanged()
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

        holder.removeButton.visibility = if (isTimerRunning) View.GONE else View.VISIBLE

        // Set checked state without triggering the listener
        holder.checkbox.setOnCheckedChangeListener(null)
        holder.checkbox.isChecked = item.isChecked

        // **THE FIX**: Save the checked state to the database
        holder.checkbox.setOnCheckedChangeListener { _, isChecked ->
            item.isChecked = isChecked
            repository.updateTaskStatus(item.id, isChecked) // Save state
        }

        holder.removeButton.setOnClickListener {
            val currentPosition = holder.adapterPosition
            if (currentPosition != RecyclerView.NO_POSITION) {
                val itemToRemove = items[currentPosition]
                repository.deleteTask(itemToRemove.id)
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