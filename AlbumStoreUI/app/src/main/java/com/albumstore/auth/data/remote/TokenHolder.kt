package com.albumstore.auth.data.remote

data class TokenHolder(
    val token: String?, // This should match `result.token`
    val isNewUser: Boolean,
    val user: UserDetails?
)
data class LoginResponse(
    val result: TokenHolder?, // This maps to the `result` object in your JSON
    val errors: Map<String, List<String>>, // Empty map in your example but could have errors
    val isValid: Boolean
)
