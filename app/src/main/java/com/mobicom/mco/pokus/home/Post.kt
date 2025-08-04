package com.mobicom.mco.pokus.home

import com.mobicom.mco.pokus.todo.TodoItem

data class Post(
    val email: String,
    val id: String,
    val name: String,
    val date: String,
    val title: String,
    val content: String,
    val timeSpent: String,
    val todoList: ArrayList<TodoItem>,
    var isLiked: Boolean = false,
    var likes: Int = 0,
    var comments: ArrayList<Comment> = ArrayList()
)

data class Comment(
    val username: String,
    val comment: String
)
