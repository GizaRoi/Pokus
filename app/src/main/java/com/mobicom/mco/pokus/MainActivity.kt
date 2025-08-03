package com.mobicom.mco.pokus

import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mobicom.mco.pokus.databinding.ActivityMainBinding
import com.mobicom.mco.pokus.home.HomeFragment
import com.mobicom.mco.pokus.search.SearchFragment
import com.mobicom.mco.pokus.todo.TodoFragment
import com.mobicom.mco.pokus.sessions.SessionsFragment
import com.mobicom.mco.pokus.profile.ProfileFragment
import com.mobicom.mco.pokus.home.Post
import com.mobicom.mco.pokus.todo.TodoItem


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    companion object {

        val todoList = mutableListOf<TodoItem>()

        const val PREFS_NAME = "UserPrefs"
        const val KEY_NAME = "name"
        const val KEY_BIO = "bio"
        const val KEY_LINK = "link"
        const val KEY_PFP = "pfpURL"

        var currentUsername = "reever"
        var currentBio: String = "CS student trying to survive finals. Focused on mobile development and AI. Let's get this bread!"
        var currentLink: String = "instagram.com/roimark"
        var currentProfilePictureUrl: String = "panda"
        var currentProfilePictureRes: Int = R.drawable.ic_default_profile
        val currentDate = "A day ago"
        val currentTitle = "Welcome Back!"
        val currentContent = "Your latest updates are shown here."
        val currentTimeSpent = "Time: 1h 40m"
        val currentTodoList = listOf("✔ Task A", "✔ Task B", "✔ Task C")
        val currentComments = listOf("You got this!", "Proud of you 💪")

        val dummyPost =
            Post(
                name = currentUsername,
                date = currentDate,
                title = currentTitle,
                content = currentContent,
                imageResId = currentProfilePictureRes,
                timeSpent = currentTimeSpent,
                todoList = currentTodoList,
                comments = currentComments
            )

        val dummyPosts = listOf(dummyPost, dummyPost, dummyPost)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)


        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        fetchUserData()

        // Default fragment* to change later with log in
        loadFragment(HomeFragment())
        setContentView(binding.root)
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> loadFragment(HomeFragment())
                R.id.nav_search -> loadFragment(SearchFragment())
                R.id.nav_todo -> loadFragment(TodoFragment())
                R.id.nav_sessions -> loadFragment(SessionsFragment())
                R.id.nav_profile -> loadFragment(ProfileFragment())

            }
            true
        }
    }

    private fun fetchUserData() {
        val userId = firebaseAuth.currentUser?.email ?: return
        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    currentUsername = document.getString(KEY_NAME) ?: currentUsername
                    currentBio = document.getString(KEY_BIO) ?: currentBio
                    currentLink = document.getString(KEY_LINK) ?: currentLink
                    currentProfilePictureUrl = document.getString(KEY_PFP) ?: "panda"
                    currentProfilePictureRes = getDrawableIdByName(currentProfilePictureUrl)


                } else {
                    Log.d("MainActivity", "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.d("MainActivity", "Get failed with ", exception)
            }
    }

    private fun getDrawableIdByName(drawableName: String): Int {
        return try {
            // 'packageName' is available directly in an Activity
            resources.getIdentifier(drawableName, "drawable", packageName)
        } catch (e: Resources.NotFoundException) { // Though getIdentifier usually returns 0 instead of throwing
            0 // Return 0 if not found
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}
