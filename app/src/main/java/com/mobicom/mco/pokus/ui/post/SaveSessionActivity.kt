package com.mobicom.mco.pokus.ui.post

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mobicom.mco.pokus.MainActivity
import com.mobicom.mco.pokus.R
import com.mobicom.mco.pokus.databinding.ActivitySaveSessionBinding
import com.mobicom.mco.pokus.home.Post
import com.mobicom.mco.pokus.todo.TodoItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class SaveSessionActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySaveSessionBinding
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    // Variable to hold the list of completed tasks
    private lateinit var completedTasks: ArrayList<TodoItem>
    private var durationInMillis: Long = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySaveSessionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get data from the Intent
        durationInMillis = intent.getLongExtra("SESSION_DURATION", 0L)
        val tasksDoneCount = intent.getIntExtra("TASKS_DONE_COUNT", 0)
        val totalTasksCount = intent.getIntExtra("TOTAL_TASKS_COUNT", 0)
        completedTasks = intent.getParcelableArrayListExtra("COMPLETED_TASKS_LIST") ?: arrayListOf()

        // Display the data
        binding.tvDuration.text = "${formatDuration(durationInMillis)}\nDuration"
        binding.tvTasksDone.text = "$tasksDoneCount / $totalTasksCount\nTasks Done"
        binding.tvDateTime.text = SimpleDateFormat("dd MMMM yyyy, hh:mm a", Locale.getDefault()).format(Date())

        val visibilityAdapter = ArrayAdapter.createFromResource(
            this, R.array.visibility_options, android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerVisibility.adapter = adapter
        }

        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.btnDiscard.setOnClickListener { finish() }
        binding.btnSavePost.setOnClickListener { savePostToFirebase() }
    }

    private fun savePostToFirebase() {
        val userId = firebaseAuth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "You must be logged in to post.", Toast.LENGTH_SHORT).show()
            return
        }

        // **THE FIX**: Convert the list of TodoItem objects to a list of Strings (titles)
        val completedTaskTitles = completedTasks.map { it.title }

        val post = Post(
            // Firestore will generate the ID, we can leave this blank for a new post
            userId = userId,
            name = MainActivity.currentUsername,
            title = binding.sessionTitleLabel.text.toString(), // Or get from an EditText if you add one
            content = binding.etDescription.text.toString(),
            date = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(Date()),
            durationMillis = this.durationInMillis, // Save the raw milliseconds
            todoList = completedTaskTitles // Use the converted list of strings
        )

        firestore.collection("posts")
            .add(post)
            .addOnSuccessListener {
                Toast.makeText(this, "Session posted successfully!", Toast.LENGTH_SHORT).show()
                navigateToHome()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error saving post: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun navigateToHome() {
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