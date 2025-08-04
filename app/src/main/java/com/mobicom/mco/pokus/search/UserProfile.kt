package com.mobicom.mco.pokus.search

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.mobicom.mco.pokus.MainActivity
import com.mobicom.mco.pokus.R
import com.mobicom.mco.pokus.models.User

class UserProfile : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user)

        val backIcon: ImageView = findViewById(R.id.backIcon)
        backIcon.setOnClickListener {
            finish()
        }

        // Get the username passed from the search screen
        val usernameToShow = intent.getStringExtra("USERNAME")

        if (usernameToShow != null) {
            // Call the new function in MainActivity to fetch this specific user's data
            MainActivity.fetchUserProfileByUsername(usernameToShow) { user ->
                if (user != null) {
                    // If the user is found, populate the UI
                    populateUi(user)
                } else {
                    // Handle case where user is not found
                    Toast.makeText(this, "User '$usernameToShow' not found.", Toast.LENGTH_LONG).show()
                    finish()
                }
            }
        } else {
            Toast.makeText(this, "No user specified.", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun populateUi(user: User) {
        val usernameText: TextView = findViewById(R.id.username)
        val bioText: TextView = findViewById(R.id.userBio)
        val linkText: TextView = findViewById(R.id.userLink)
        // Note: The User model and UI for stats (sessions, followers) will need to be updated
        // to show real data, but this sets up the main profile info.

        usernameText.text = user.username
        bioText.text = user.bio
        linkText.text = user.links

        // You would add logic here to load the profile picture from user.pfpURL
        // using an image loading library like Glide or Picasso.
    }
}