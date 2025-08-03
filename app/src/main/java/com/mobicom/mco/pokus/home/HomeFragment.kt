package com.mobicom.mco.pokus.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mobicom.mco.pokus.R
import com.mobicom.mco.pokus.home.Post
import com.mobicom.mco.pokus.home.PostAdapter

class HomeFragment : Fragment() {

    private lateinit var postRecycler: RecyclerView
    private lateinit var postAdapter: PostAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.activity_home, container, false)

        postRecycler = view.findViewById(R.id.postRecycler)
        postRecycler.layoutManager = LinearLayoutManager(requireContext())

        val dummyPosts = listOf(
            Post(
                name = "Roimarc",
                date = "1 day ago",
                title = "MOBICOM + STCLOUD",
                content = "Finished backlogs and review materials",
                imageResId = R.drawable.sample_pic,
                timeSpent = "Time: 1h 32m",
                todoList = listOf(
                    "✔ STCLOUD Mini-Project 2",
                    "✔ MOBICOM Exercise #2",
                    "✔ Android Challenge Review",
                    "✔ STCLOUD Exam #2 Review"
                ),
                comments = listOf(
                    "This is so productive!",
                    "Teach me your time management skills 😭",
                    "Good luck sa exams!"
                )
            ),
            Post(
                name = "peter",
                date = "2 days ago",
                title = "STCLOUD Notes",
                content = "Taking notes made this easier.",
                imageResId = R.drawable.sample_pic,
                timeSpent = "Time: 1h 32m",
                todoList = listOf(
                    "✔ STCLOUD Mini-Project 2",
                    "✔ MOBICOM Exercise #2",
                    "✔ Android Challenge Review",
                    "✔ STCLOUD Exam #2 Review"
                ),
                comments = listOf(
                    "This is so productive!",
                    "Teach me your time management skills 😭",
                    "Good luck sa exams!"
                )
            ),
            Post(
                name = "reever",
                date = "3 days ago",
                title = "Catching Up",
                content = "Reviewing for exams today.",
                imageResId = R.drawable.sample_pic,
                timeSpent = "Time: 1h 32m",
                todoList = listOf(
                    "✔ STCLOUD Mini-Project 2",
                    "✔ MOBICOM Exercise #2",
                    "✔ Android Challenge Review",
                    "✔ STCLOUD Exam #2 Review"
                ),
                comments = listOf(
                    "This is so productive!",
                    "Teach me your time management skills 😭",
                    "Good luck sa exams!"
                )
            )
        )

        postAdapter = PostAdapter(dummyPosts)
        postRecycler.adapter = postAdapter

        return view
    }
}
