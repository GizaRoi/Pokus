package com.mobicom.mco.pokus.home

import com.mobicom.mco.pokus.todo.TodoItem


// Post model to match Firestore structure
data class Post(
    var id: String = "",
    val name: String = "", // username of the author
    val userId: String = "", // Firebase Auth UID
    val date: String = "",
    val email: String = "",
    val timeSpent: String = "",
    val title: String = "",
    val content: String = "",
    val durationMillis: Long = 0L, // Storing duration as milliseconds
    val likes: Int = 0,
    val todoList: List<String> = emptyList(), // Storing only the titles as Strings
    var isLiked: Boolean = false
)

data class Comment(
    val username: String,
    val comment: String
)
