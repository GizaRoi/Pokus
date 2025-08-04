package com.mobicom.mco.pokus.todo

data class TodoItem(
    val id: Long = 0,
    val title: String,
    var isChecked: Boolean = false
)