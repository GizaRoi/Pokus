package com.mobicom.mco.pokus.search

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.mobicom.mco.pokus.MainActivity
import com.mobicom.mco.pokus.R

class UserProfile : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user)

        // View bindings
        val usernameText: TextView = findViewById(R.id.username)
        val bioText: TextView = findViewById(R.id.userBio)
        val linkText: TextView = findViewById(R.id.userLink)
        val sessionCountText: TextView = findViewById(R.id.sessionCount)
        val followersCountText: TextView = findViewById(R.id.followersCount)
        val followingCountText: TextView = findViewById(R.id.followingCount)
        val followButton: Button = findViewById(R.id.followButton)
        val backIcon: ImageView = findViewById(R.id.backIcon)

        // Get the username from the intent to know which profile to display
        val usernameToShow = intent.getStringExtra("USERNAME")

        // For this simple implementation, we'll assume we are only viewing
        // the currently logged-in user's profile, as that's the data MainActivity fetches.
        // A more complex app would fetch the specific user's data here.
        if (usernameToShow == MainActivity.currentUsername) {
            populateUiWithCurrentUserData()
        } else {
            // In a real app, you would fetch the data for 'usernameToShow' here.
            // For now, we'll show a message or the current user's data as a fallback.
            Toast.makeText(this, "Viewing your own profile.", Toast.LENGTH_SHORT).show()
            populateUiWithCurrentUserData()
        }

        followButton.setOnClickListener {
            Toast.makeText(this, "Follow button clicked for ${usernameText.text}", Toast.LENGTH_SHORT).show()
        }

        backIcon.setOnClickListener {
            finish()
        }
    }

    private fun populateUiWithCurrentUserData() {
        // Find views again or pass them as parameters
        val usernameText: TextView = findViewById(R.id.username)
        val bioText: TextView = findViewById(R.id.userBio)
        val linkText: TextView = findViewById(R.id.userLink)

        // Populate the UI using the static variables from MainActivity
        usernameText.text = MainActivity.currentUsername
        bioText.text = MainActivity.currentBio
        linkText.text = MainActivity.currentLink

        // You can add the other fields like school, stats etc. here if they are
        // stored in MainActivity's companion object as well.
    }
}