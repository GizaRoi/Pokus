package com.mobicom.mco.pokus.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.mobicom.mco.pokus.MainActivity
import com.mobicom.mco.pokus.MainActivity.Companion.posts
import com.mobicom.mco.pokus.databinding.ActivityHomeBinding


class HomeFragment : Fragment() {

    private var _binding: ActivityHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: PostAdapter
    private var firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ActivityHomeBinding.inflate(inflater, container, false)

        adapter = PostAdapter(MainActivity.posts)

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun fetchPosts() {
        firestore.collection("posts")
            .get()
            .addOnSuccessListener { documents ->
                MainActivity.posts = documents.map { doc ->
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
                Log.e("HomeFragment", "Error fetching posts: ", exception)
            }
    }

    override fun onResume() {
        super.onResume()
        fetchPosts()
    }
}
