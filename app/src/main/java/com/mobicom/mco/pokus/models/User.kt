package com.mobicom.mco.pokus.models

data class User(
    val id: String = "",
    var username: String = "",
    var pfpURL: String? = null,
    var bio: String = "",
    var school: String = "",
    var links: String = ""
)