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
        val profileImage: ImageView = findViewById(R.id.profileImage)
        val usernameText: TextView = findViewById(R.id.username)
        val bioText: TextView = findViewById(R.id.userBio)
        val linkText: TextView = findViewById(R.id.userLink)
        val sessionCountText: TextView = findViewById(R.id.sessionCount)
        val followersCountText: TextView = findViewById(R.id.followersCount)
        val followingCountText: TextView = findViewById(R.id.followingCount)
        val followButton: Button = findViewById(R.id.followButton)
        val backIcon: ImageView = findViewById(R.id.backIcon)

        // Get the username from intent
        val username = intent.getStringExtra("USERNAME") ?: "Unknown"
        usernameText.text = username

        // Load data from MainActivity
        val bio = MainActivity.userBios[username] ?: "No bio available."
        val link = MainActivity.userLinks[username] ?: "N/A"
        val stats = MainActivity.userStats[username] ?: Triple(0, 0, 0)

        // Set bio and link
        bioText.text = bio
        linkText.text = link

        // Set session and following count
        sessionCountText.text = stats.first.toString()
        followingCountText.text = stats.third.toString()

        // Initialize follower logic
        var isFollowing = false
        var currentFollowers = stats.second
        followersCountText.text = currentFollowers.toString()

        followButton.setOnClickListener {
            isFollowing = !isFollowing
            followButton.text = if (isFollowing) "Following" else "Follow"

            // Update followers count
            currentFollowers += if (isFollowing) 1 else -1
            followersCountText.text = currentFollowers.toString()

            Toast.makeText(
                this,
                if (isFollowing) "You followed $username" else "Unfollowed $username",
                Toast.LENGTH_SHORT
            ).show()
        }

        // Back icon behavior
        backIcon.setOnClickListener {
            finish()
        }
    }
}
