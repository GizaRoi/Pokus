package com.mobicom.mco.pokus

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
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

    companion object {

        val todoList = mutableListOf<TodoItem>()

        const val PREFS_NAME = "UserPrefs"
        const val KEY_NAME = "name"
        const val KEY_BIO = "bio"
        const val KEY_LINK = "link"

        var currentUsername = "reever"
        var currentBio: String = "CS student trying to survive finals. Focused on mobile development and AI. Let's get this bread!"
        var currentLink: String = "instagram.com/roimark"

        var currentProfilePicRes = R.drawable.profile_pic
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
                imageResId = currentProfilePicRes,
                timeSpent = currentTimeSpent,
                todoList = currentTodoList,
                comments = currentComments
            )

        val dummyPosts = listOf(dummyPost, dummyPost, dummyPost)


    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        currentUsername = prefs.getString(KEY_NAME, currentUsername) ?: currentUsername
        currentBio = prefs.getString(KEY_BIO, currentBio) ?: currentBio
        currentLink = prefs.getString(KEY_LINK, currentLink) ?: currentLink

        // Default fragment* to change later with log in
        loadFragment(HomeFragment())

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

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}
