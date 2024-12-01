package com.albumstore.core.data

data class UserPreferences(
    val username: String = "",
    val token: String = "",
    val roles: String = "",
    val userId: String = ""
)