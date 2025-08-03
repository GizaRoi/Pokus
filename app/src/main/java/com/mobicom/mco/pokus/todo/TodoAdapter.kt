package com.mobicom.mco.pokus.todo

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mobicom.mco.pokus.R


class TodoAdapter(
    private val items: MutableList<TodoItem>
) : RecyclerView.Adapter<TodoAdapter.ToDoViewHolder>() {

    inner class ToDoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val checkbox: CheckBox = itemView.findViewById(R.id.checkbox)
        val title: TextView = itemView.findViewById(R.id.todoTitle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ToDoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.itemtodo, parent, false)
        return ToDoViewHolder(view)
    }

    override fun onBindViewHolder(holder: ToDoViewHolder, position: Int) {
        val item = items[position]
        holder.title.text = item.title
        holder.checkbox.isChecked = item.isChecked

        // Avoid triggering listener during recycling
        holder.checkbox.setOnCheckedChangeListener(null)
        holder.checkbox.isChecked = item.isChecked

        // Set up delete-on-check
        holder.checkbox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                removeItem(holder.adapterPosition)
            } else {
                item.isChecked = false
            }
        }
    }

    fun removeItem(position: Int) {
        if (position != RecyclerView.NO_POSITION) {
            items.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    override fun getItemCount(): Int = items.size

    fun addItem(title: String) {
        items.add(TodoItem(title))
        notifyItemInserted(items.size - 1)
    }
}
