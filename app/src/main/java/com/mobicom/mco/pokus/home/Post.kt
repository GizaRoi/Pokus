package com.mobicom.mco.pokus.home

data class Post(
    val name: String,
    val date: String,
    val title: String,
    val content: String,
    val timeSpent: String,
    val todoList: List<String>,
    var isLiked: Boolean = false,
    var commentUsernames: MutableList<String> = mutableListOf(),
    var comments: MutableList<String> = mutableListOf()
)
