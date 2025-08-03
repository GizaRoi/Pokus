package com.mobicom.mco.pokus.todo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mobicom.mco.pokus.R
import com.mobicom.mco.pokus.MainActivity

class TodoFragment : Fragment() {

    private lateinit var adapter: TodoAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var fab: FloatingActionButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the fragment layout
        return inflater.inflate(R.layout.activity_todo, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.todoRecyclerView)
        fab = view.findViewById(R.id.fab_add)

        adapter = TodoAdapter(MainActivity.todoList)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        fab.setOnClickListener {
            showAddTaskDialog()
        }
    }

    private fun showAddTaskDialog() {
        val input = EditText(requireContext())
        AlertDialog.Builder(requireContext())
            .setTitle("Add Task")
            .setMessage("What do you want to add?")
            .setView(input)
            .setPositiveButton("Add") { _, _ ->
                val task = input.text.toString()
                if (task.isNotEmpty()) {
                    adapter.addItem(task)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
