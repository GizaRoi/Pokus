package com.mobicom.mco.pokus.data.database

import android.content.ContentValues
import android.content.Context
import com.mobicom.mco.pokus.todo.TodoItem

class TodoDao(context: Context) {

    private val dbHelper = PokusDatabaseHelper(context)

    // Function to add a new task to the database
    fun addTask(task: TodoItem): Long {
        val db = dbHelper.writableDatabase
        val contentValues = ContentValues().apply {
            put(PokusDatabaseHelper.COLUMN_TITLE, task.title)
            put(PokusDatabaseHelper.COLUMN_IS_CHECKED, if (task.isChecked) 1 else 0)
        }
        val id = db.insert(PokusDatabaseHelper.TABLE_TASKS, null, contentValues)
        db.close()
        return id
    }

    // Function to get all tasks from the database
    fun getAllTasks(): MutableList<TodoItem> {
        val tasks = mutableListOf<TodoItem>()
        val db = dbHelper.readableDatabase
        val cursor = db.query(PokusDatabaseHelper.TABLE_TASKS, null, null, null, null, null, null)

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow(PokusDatabaseHelper.COLUMN_ID))
                val title = cursor.getString(cursor.getColumnIndexOrThrow(PokusDatabaseHelper.COLUMN_TITLE))
                val isChecked = cursor.getInt(cursor.getColumnIndexOrThrow(PokusDatabaseHelper.COLUMN_IS_CHECKED)) == 1

                tasks.add(TodoItem(id, title, isChecked))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return tasks
    }

    // Function to update a task's status (e.g., when it's checked off)
    fun updateTaskStatus(id: Long, isChecked: Boolean) {
        val db = dbHelper.writableDatabase
        val contentValues = ContentValues().apply {
            put(PokusDatabaseHelper.COLUMN_IS_CHECKED, if (isChecked) 1 else 0)
        }
        db.update(
            PokusDatabaseHelper.TABLE_TASKS,
            contentValues,
            "${PokusDatabaseHelper.COLUMN_ID} = ?",
            arrayOf(id.toString())
        )
        db.close()
    }

    // Function to delete a task from the database
    fun deleteTask(id: Long) {
        val db = dbHelper.writableDatabase
        db.delete(
            PokusDatabaseHelper.TABLE_TASKS,
            "${PokusDatabaseHelper.COLUMN_ID} = ?",
            arrayOf(id.toString())
        )
        db.close()
    }
}