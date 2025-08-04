package com.mobicom.mco.pokus.todo

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

class TodoAdapter(
    private var items: MutableList<TodoItem>,
    private val context: Context,
    private val onSelectionChanged: () -> Unit // Callback to notify the fragment
) : RecyclerView.Adapter<TodoAdapter.ToDoViewHolder>() {

    private val repository = PokusRepository.getInstance(context)

    inner class ToDoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val checkbox: CheckBox = itemView.findViewById(R.id.checkbox)
        val title: TextView = itemView.findViewById(R.id.todoTitle)
        // Find the remove button
        val removeButton: ImageButton = itemView.findViewById(R.id.btn_remove_task)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ToDoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.itemtodo, parent, false)
        return ToDoViewHolder(view)
    }

    override fun onBindViewHolder(holder: ToDoViewHolder, position: Int) {
        val item = items[position]
        holder.title.text = item.title

        // Hide the remove button on the To-Do screen
        holder.removeButton.visibility = View.GONE

        // Set checked state without triggering the listener
        holder.checkbox.setOnCheckedChangeListener(null)
        holder.checkbox.isChecked = item.isChecked

        // Set the listener to update the item's state and notify the fragment
        holder.checkbox.setOnCheckedChangeListener { _, isChecked ->
            item.isChecked = isChecked
            repository.updateTaskStatus(item.id, isChecked)
            onSelectionChanged() // Notify the fragment that a selection has changed
        }
    }

    fun getSelectedItems(): List<TodoItem> {
        return items.filter { it.isChecked }
    }

    fun removeItems(itemsToRemove: List<TodoItem>) {
        itemsToRemove.forEach { item ->
            repository.deleteTask(item.id)
        }
        // Remove items from the local list and update the adapter
        items.removeAll(itemsToRemove)
        notifyDataSetChanged()
    }


    override fun getItemCount(): Int = items.size

    fun addItem(item: TodoItem) {
        items.add(item)
        notifyItemInserted(items.size - 1)
    }
}