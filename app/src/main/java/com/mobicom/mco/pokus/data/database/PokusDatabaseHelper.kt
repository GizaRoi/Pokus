package com.mobicom.mco.pokus.data.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.mobicom.mco.pokus.todo.TodoItem

class PokusDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_VERSION = 1 // Change to 2 if you've run the app before
        private const val DATABASE_NAME = "Pokus.db"

        // Constants for tasks table
        const val TABLE_TASKS = "tasks"
        const val COLUMN_ID = "id"
        const val COLUMN_TITLE = "title"
        const val COLUMN_IS_CHECKED = "is_checked"

        // NEW: Constants for session_tasks table
        const val TABLE_SESSION_TASKS = "session_tasks"
        const val COLUMN_SESSION_TASK_ID = "task_id"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTaskTableQuery = ("CREATE TABLE $TABLE_TASKS ("
                + "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "$COLUMN_TITLE TEXT,"
                + "$COLUMN_IS_CHECKED INTEGER)")
        db?.execSQL(createTaskTableQuery)

        // NEW: Create the session_tasks table
        val createSessionTaskTableQuery = ("CREATE TABLE $TABLE_SESSION_TASKS ("
                + "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "$COLUMN_SESSION_TASK_ID INTEGER)")
        db?.execSQL(createSessionTaskTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_TASKS")
        // NEW: Drop the session_tasks table on upgrade
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_SESSION_TASKS")
        onCreate(db)
    }
}