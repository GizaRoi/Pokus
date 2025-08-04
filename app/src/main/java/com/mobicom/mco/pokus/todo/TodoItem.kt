package com.mobicom.mco.pokus.todo

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class TodoItem(
    val id: Long = 0,
    val title: String,
    var isChecked: Boolean = false
) : Parcelable