package com.mobicom.mco.pokus.ui.post

import android.content.Intent
import com.mobicom.mco.pokus.home.Post
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mobicom.mco.pokus.MainActivity
import com.mobicom.mco.pokus.R
import com.mobicom.mco.pokus.databinding.ActivitySaveSessionBinding
import com.mobicom.mco.pokus.todo.TodoItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import java.util.concurrent.TimeUnit

class SaveSessionActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySaveSessionBinding
    private var firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
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

        // Set listener for the Discard button
        binding.btnDiscard.setOnClickListener {
            finish()
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
        val db = FirebaseFirestore.getInstance()
        val post = Post(
            id = UUID.randomUUID().toString(),
            name = MainActivity.currentUsername,
            title = "Session Summary",
            content = "${binding.etDescription.text}",
            date = binding.tvDateTime.text.toString(),
            email = firebaseAuth.currentUser?.uid ?: "unknown_user",
            timeSpent = binding.tvDuration.text.toString(),
            todoList = completedTasks,
        )
        firebaseAuth.currentUser?.email?.let {
            db.collection("posts")
                .add(post)
                .addOnSuccessListener {
                    Log.d("SavePostActivity", "Post added with ID: ${it.id}")
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error saving post: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }

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