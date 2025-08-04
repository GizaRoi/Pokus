package com.mobicom.mco.pokus

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
import java.util.Date


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private var firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    companion object{
        lateinit var currentUsername: String
        lateinit var currentBio: String
        lateinit var currentLink: String
        var currentPfp: String = ""
        lateinit var currentSchool: String
        const val PREFS_NAME = "UserPrefs"
        const val KEY_NAME = "username"
        const val KEY_BIO = "bio"
        const val KEY_LINK = "link"
        var posts: ArrayList<Post> = ArrayList()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        currentUsername = prefs.getString(KEY_NAME, currentUsername) ?: currentUsername
        currentBio = prefs.getString(KEY_BIO, currentBio) ?: currentBio
        currentLink = prefs.getString(KEY_LINK, currentLink) ?: currentLink
        currentPfp = prefs.getString("pfpURL", currentPfp) ?: currentPfp
        currentSchool = prefs.getString("school", currentSchool) ?: currentSchool
        fetchUserData()
        fetchPosts()

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

    private fun fetchUserData(){
        firestore.collection("users")
            .document(firebaseAuth.currentUser?.email?: "")
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    currentUsername = document.getString("username") ?: currentUsername
                    currentBio = document.getString("bio") ?: currentBio
                    currentLink = document.getString("link") ?: currentLink
                    currentPfp = document.getString("pfpURL") ?: currentPfp
                    currentSchool = document.getString("school") ?: currentSchool

                    // Save to SharedPreferences
                    val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                    with(prefs.edit()) {
                        putString(KEY_NAME, currentUsername)
                        putString(KEY_BIO, currentBio)
                        putString(KEY_LINK, currentLink)
                        putString("pfpURL", currentPfp)
                        putString("school", currentSchool)
                        apply()
                    }
                }
            }
    }

    private fun fetchPosts() {
        firestore.collection("posts")
            .get()
            .addOnSuccessListener { documents ->
                posts = documents.map { doc ->
                    Post(
                        id = doc.id,
                        title = doc.getString("title") ?: "No Title",
                        email = doc.getString("email") ?: "unknown",
                        likes = doc.getLong("likes")?.toInt() ?: 0,
                        content = doc.getString("content") ?: "",
                        name = doc.getString("author") ?: "",
                        timeSpent = doc.getDate("timeSpent").toString(),
                        date = doc.getDate("date")?.toString() ?: Date().toString(),
                        todoList = (doc.get("todoList") as? List<Map<String, Any>>)?.map { todoMap ->
                            TodoItem(
                                id = todoMap["id"] as Long,
                                title = todoMap["title"] as String,
                                isChecked= todoMap["isCompleted"] as Boolean
                            )
                        }?.toCollection(ArrayList()) ?: ArrayList(),
                    )
                } as ArrayList<Post>
            }
            .addOnFailureListener { exception ->
                Log.e("MainActivity", "Error fetching posts: ", exception)
            }
    }
}
