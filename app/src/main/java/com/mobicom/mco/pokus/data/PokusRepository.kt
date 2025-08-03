package com.mobicom.mco.pokus.data.repository

import android.content.Context
import com.mobicom.mco.pokus.data.database.TodoDao
import com.mobicom.mco.pokus.todo.TodoItem

class PokusRepository private constructor(context: Context) {

    private val todoDao = TodoDao(context)

    // Public methods that the UI will call
    fun addTask(task: TodoItem) = todoDao.addTask(task)
    fun getAllTasks(): MutableList<TodoItem> = todoDao.getAllTasks()
    fun updateTaskStatus(id: Long, isChecked: Boolean) = todoDao.updateTaskStatus(id, isChecked)
    fun deleteTask(id: Long) = todoDao.deleteTask(id)

    companion object {
        private var INSTANCE: PokusRepository? = null

        fun getInstance(context: Context): PokusRepository {
            if (INSTANCE == null) {
                INSTANCE = PokusRepository(context)
            }
            return INSTANCE!!
        }
    }
}