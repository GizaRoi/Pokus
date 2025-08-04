package com.mobicom.mco.pokus.todo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mobicom.mco.pokus.R
import com.mobicom.mco.pokus.data.repository.PokusRepository
import com.mobicom.mco.pokus.sessions.SessionDataHolder

class TodoFragment : Fragment() {

    private lateinit var adapter: TodoAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var fab: FloatingActionButton
    private lateinit var repository: PokusRepository
    private lateinit var todoList: MutableList<TodoItem>

    // Views for the new action buttons
    private lateinit var actionsLayout: LinearLayout
    private lateinit var completeButton: Button
    private lateinit var importButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_todo, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        repository = PokusRepository.getInstance(requireContext())
        recyclerView = view.findViewById(R.id.todoRecyclerView)
        fab = view.findViewById(R.id.fab_add)
        actionsLayout = view.findViewById(R.id.actionsLayout)
        completeButton = view.findViewById(R.id.btn_complete)
        importButton = view.findViewById(R.id.btn_import)

        todoList = repository.getAllTasks()

        // Pass a callback to the adapter
        adapter = TodoAdapter(todoList, requireContext()) {
            updateActionButtonsVisibility()
        }
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        fab.setOnClickListener {
            showAddTaskDialog()
        }

        completeButton.setOnClickListener {
            val selectedItems = adapter.getSelectedItems()
            adapter.removeItems(selectedItems)
            updateActionButtonsVisibility() // Hide buttons after action
        }

        importButton.setOnClickListener {
            val selectedItems = adapter.getSelectedItems()

            if (selectedItems.isNotEmpty()) {
                // Untick the items in the list and update the database
                selectedItems.forEach { item ->
                    item.isChecked = false
                    repository.updateTaskStatus(item.id, false)
                }

                // Store the items for the SessionFragment to pick up
                SessionDataHolder.tasksToImport = selectedItems

                // Refresh the adapter to show the items are now unticked
                adapter.notifyDataSetChanged()

                // Hide the action buttons
                updateActionButtonsVisibility()

                // Switch to the Sessions tab
                activity?.findViewById<BottomNavigationView>(R.id.bottomNav)?.selectedItemId = R.id.nav_sessions
            }
        }

        updateActionButtonsVisibility() // Initial check
    }

    private fun updateActionButtonsVisibility() {
        val selectedCount = adapter.getSelectedItems().size
        actionsLayout.visibility = if (selectedCount > 0) View.VISIBLE else View.GONE
    }

    private fun showAddTaskDialog() {
        val input = EditText(requireContext())
        AlertDialog.Builder(requireContext())
            .setTitle("Add Task")
            .setMessage("What do you want to add?")
            .setView(input)
            .setPositiveButton("Add") { _, _ ->
                val taskTitle = input.text.toString()
                if (taskTitle.isNotEmpty()) {
                    val newTask = TodoItem(title = taskTitle, isChecked = false)
                    val newId = repository.addTask(newTask)
                    val itemWithId = newTask.copy(id = newId)
                    adapter.addItem(itemWithId)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}