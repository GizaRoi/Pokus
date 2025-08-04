package com.mobicom.mco.pokus

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mobicom.mco.pokus.databinding.ActivityMainBinding
import com.mobicom.mco.pokus.home.HomeFragment
import com.mobicom.mco.pokus.search.SearchFragment
import com.mobicom.mco.pokus.todo.TodoFragment
import com.mobicom.mco.pokus.sessions.SessionsFragment
import com.mobicom.mco.pokus.profile.ProfileFragment
import com.mobicom.mco.pokus.home.Post
import com.mobicom.mco.pokus.models.User


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
        const val KEY_PFP = "pfpURL"
        const val KEY_SCHOOL = "school"
        var posts: ArrayList<Post> = ArrayList()

        fun fetchUserProfileByUsername(username: String, callback: (User?) -> Unit) {
            val firestore = FirebaseFirestore.getInstance()
            firestore.collection("users")
                .whereEqualTo("username", username)
                .limit(1)
                .get()
                .addOnSuccessListener { documents ->
                    if (documents.isEmpty) {
                        callback(null) // User not found
                    } else {
                        // Convert the document to our User object
                        val user = documents.documents[0].toObject(User::class.java)
                        callback(user)
                    }
                }
                .addOnFailureListener {
                    callback(null) // Handle failure
                }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        currentUsername = prefs.getString(KEY_NAME, "Guest") ?: "Guest"
        currentBio = prefs.getString(KEY_BIO, "No bio yet.") ?: "No bio yet."
        currentLink = prefs.getString(KEY_LINK, "") ?: ""
        currentPfp = prefs.getString(KEY_PFP, "astronaut") ?: "astronaut"
        currentSchool = prefs.getString(KEY_SCHOOL, "") ?: ""

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
                        name = doc.getString("name") ?: "",
                        timeSpent = doc.getString("timeSpent") ?: "0m",
                        date = doc.getString("date") ?: "",
                        todoList = doc.get("todoList") as? ArrayList<String> ?: ArrayList(),
                    )
                } as ArrayList<Post>
            }
            .addOnFailureListener { exception ->
                Log.e("MainActivity", "Error fetching posts: ", exception)
            }
    }

    fun fetchPostsForUser(username: String, callback: (ArrayList<Post>) -> Unit) {
        firestore.collection("posts")
            .whereEqualTo("name", username) // Query for posts where the 'name' field matches the username
            .get()
            .addOnSuccessListener { documents ->
                val userPosts = ArrayList<Post>()
                for (doc in documents) {
                    val post = doc.toObject(Post::class.java)
                    post.id = doc.id
                    userPosts.add(post)
                }
                callback(userPosts) // Return the list of posts via the callback
            }
            .addOnFailureListener { exception ->
                Log.e("MainActivity", "Error fetching posts for user $username: ", exception)
                callback(ArrayList()) // Return an empty list on failure
            }
    }
}
