package com.mobicom.mco.pokus.home

data class Post(
    val name: String,
    val date: String,
    val title: String,
    val content: String,
    val imageResId: Int,
    val timeSpent: String,
    val todoList: List<String>,
    val comments: List<String>
)