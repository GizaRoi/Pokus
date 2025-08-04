package com.mobicom.mco.pokus.models

data class User(
    val id: String = "",
    var username: String = "",
    var pfpURL: String? = "ic_default_profile",
    var bio: String = "",
    var school: String = "",
    var links: String = ""
)