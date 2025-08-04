package com.mobicom.mco.pokus.search

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mobicom.mco.pokus.MainActivity
import com.mobicom.mco.pokus.R
import com.mobicom.mco.pokus.models.User
import com.mobicom.mco.pokus.home.PostAdapter

class UserProfile : AppCompatActivity() {

    private lateinit var postsRecyclerView: RecyclerView
    private lateinit var postAdapter: PostAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // **THE FIX**: Change this line to use the correct layout
        setContentView(R.layout.activity_user)

        findViewById<ImageView>(R.id.backIcon).setOnClickListener {
            finish()
        }

        // Set up RecyclerView
        postsRecyclerView = findViewById(R.id.postsRecyclerView)
        postAdapter = PostAdapter(emptyList())
        postsRecyclerView.layoutManager = LinearLayoutManager(this)
        postsRecyclerView.adapter = postAdapter

        val usernameToShow = intent.getStringExtra("USERNAME")

        if (usernameToShow != null) {
            // Fetch this specific user's profile info and posts
            MainActivity.fetchUserProfileByUsername(usernameToShow) { user ->
                if (user != null) {
                    populateUi(user)
                    // After populating profile, load their posts
                    loadUserPosts(user.username)
                } else {
                    Toast.makeText(this, "User '$usernameToShow' not found.", Toast.LENGTH_LONG).show()
                    finish()
                }
            }
        } else {
            Toast.makeText(this, "No user specified.", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun loadUserPosts(username: String) {
        MainActivity.fetchPostsForUser(username) { userPosts ->
            postAdapter.updatePosts(userPosts)
        }
    }

    private fun populateUi(user: User) {
        findViewById<TextView>(R.id.username).text = user.username
        findViewById<TextView>(R.id.userBio).text = user.bio
        findViewById<TextView>(R.id.userLink).text = user.links
        // You would update the other UI elements (followers, etc.) here
    }
}