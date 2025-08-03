package com.mobicom.mco.pokus.ui.post

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.mobicom.mco.pokus.MainActivity
import com.mobicom.mco.pokus.R
import com.mobicom.mco.pokus.databinding.ActivitySaveSessionBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class SaveSessionActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySaveSessionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySaveSessionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get data from the Intent
        val durationInMillis = intent.getLongExtra("SESSION_DURATION", 0L)
        val tasksDoneCount = intent.getIntExtra("TASKS_DONE_COUNT", 0)
        val totalTasksCount = intent.getIntExtra("TOTAL_TASKS_COUNT", 0)

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

        // Set listener for the back button on the toolbar
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        // Set listener for the Discard button
        binding.btnDiscard.setOnClickListener {
            finish() // Simply close the activity
        }

        // Set listener for the "Post" button
        binding.btnSavePost.setOnClickListener {
            // Backend developer will replace this with Firebase logic
            savePostToFirebase()
        }
    }

    private fun savePostToFirebase() {
        // --- THIS IS WHERE THE BACKEND LOGIC WILL GO ---
        // For now, we will simulate a successful post for the UI.

        Toast.makeText(this, "Session posted successfully!", Toast.LENGTH_SHORT).show()

        // Navigate back to MainActivity and show the HomeFragment
        val intent = Intent(this, MainActivity::class.java).apply {
            // These flags clear the activity stack and bring MainActivity to the front
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