package com.mobicom.mco.pokus.ui.post

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.mobicom.mco.pokus.MainActivity
import com.mobicom.mco.pokus.R
import com.mobicom.mco.pokus.databinding.ActivitySaveSessionBinding
import com.mobicom.mco.pokus.todo.TodoItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class SaveSessionActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySaveSessionBinding
    // Variable to hold the list of completed tasks
    private lateinit var completedTasks: ArrayList<TodoItem>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySaveSessionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get data from the Intent
        val durationInMillis = intent.getLongExtra("SESSION_DURATION", 0L)
        val tasksDoneCount = intent.getIntExtra("TASKS_DONE_COUNT", 0)
        val totalTasksCount = intent.getIntExtra("TOTAL_TASKS_COUNT", 0)
        // Retrieve the ArrayList of completed tasks
        completedTasks = intent.getParcelableArrayListExtra("COMPLETED_TASKS_LIST") ?: arrayListOf()

        // Display the data
        binding.tvDuration.text = "${formatDuration(durationInMillis)}\nDuration"
        binding.tvTasksDone.text = "$tasksDoneCount / $totalTasksCount\nTasks Done"
        binding.tvDateTime.text = SimpleDateFormat("dd MMMM yyyy, hh:mm a", Locale.getDefault()).format(Date())

        // Set up the visibility spinner
        val visibilityAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.visibility_options,
            android.R.layout.simple_spinner_item
        )
        visibilityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerVisibility.adapter = visibilityAdapter

        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        binding.btnDiscard.setOnClickListener {
            finish()
        }

        binding.btnSavePost.setOnClickListener {
            savePostToFirebase()
        }
    }

    private fun savePostToFirebase() {
        // Your backend developer can now access the 'completedTasks' ArrayList here.
        // For example:
        // val tasksToSave = completedTasks.map { it.title }
        // firebase.save(tasksToSave)

        Toast.makeText(this, "Session posted successfully!", Toast.LENGTH_SHORT).show()

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("NAVIGATE_TO", R.id.nav_home)
        }
        startActivity(intent)
        finish()
    }

    private fun formatDuration(millis: Long): String {
        val hours = TimeUnit.MILLISECONDS.toHours(millis)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60
        return String.format("%dh %02dm %02ds", hours, minutes, seconds)
    }
}