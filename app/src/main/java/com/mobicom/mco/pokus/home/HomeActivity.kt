package com.mobicom.mco.pokus.home

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mobicom.mco.pokus.R
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager



class HomeActivity : AppCompatActivity() {

    private lateinit var postRecycler: RecyclerView
    private lateinit var postAdapter: PostAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        postRecycler = findViewById(R.id.postRecycler)
        postRecycler.layoutManager = LinearLayoutManager(this)

        val dummyPosts = listOf(
            Post(
                name = "Roimark",
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
    }
}
