package com.albumstore.auth.data.remote

data class UserDetails(
    val id: String,
    val displayName: String,
    val firstName: String,
    val lastName: String,
    val token: String?, // This `token` might be null (not to be confused with the top-level token)
    val email: String,
    val roles: List<String>
)
