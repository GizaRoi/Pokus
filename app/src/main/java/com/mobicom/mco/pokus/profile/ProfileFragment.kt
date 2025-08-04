package com.mobicom.mco.pokus.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mobicom.mco.pokus.MainActivity
import com.mobicom.mco.pokus.R
import com.mobicom.mco.pokus.home.PostAdapter

class ProfileFragment : Fragment() {

    private lateinit var postsRecyclerView: RecyclerView
    private lateinit var postAdapter: PostAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.activity_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up static profile info
        view.findViewById<TextView>(R.id.username).text = MainActivity.currentUsername
        view.findViewById<TextView>(R.id.bio).text = MainActivity.currentBio
        view.findViewById<TextView>(R.id.link).text = MainActivity.currentLink

        // Set up RecyclerView
        postsRecyclerView = view.findViewById(R.id.postsRecyclerView)
        postAdapter = PostAdapter(emptyList()) // Start with an empty list
        postsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        postsRecyclerView.adapter = postAdapter

        // Fetch this user's posts from Firestore
        loadUserPosts(MainActivity.currentUsername)

        view.findViewById<ImageView>(R.id.settingsIcon).setOnClickListener {
            val intent = Intent(requireContext(), EditProfileActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loadUserPosts(username: String) {
        MainActivity.fetchPostsForUser(username) { userPosts ->
            // When the posts are fetched, update the adapter.
            postAdapter.updatePosts(userPosts)
        }
    }
}