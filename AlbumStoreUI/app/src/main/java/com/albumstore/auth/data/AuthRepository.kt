package com.albumstore.auth.data.remote

import android.util.Log
import com.albumstore.auth.TAG
import com.albumstore.core.data.remote.Api

class AuthRepository(private val authDataSource: AuthDataSource) {

    init {
        Log.d(TAG, "AuthRepository initialized")
    }

    // Clear the token from the interceptor
    fun clearToken() {
        Api.tokenInterceptor.token = null
    }

    // Login function to handle user authentication
    suspend fun login(username: String, password: String): Result<TokenHolder> {
        val user = User(username, password)
        val responseResult = authDataSource.login(user)

        return if (responseResult.isSuccess) {
            val loginResponse = responseResult.getOrNull()
            val token = loginResponse?.result?.token // Access the token from `result`
            val roles = loginResponse?.result?.user?.roles?.joinToString(",") ?: ""
            if (!token.isNullOrEmpty()) {
                Api.tokenInterceptor.token = token
//                Api.tokenInterceptor.roles = roles
                Log.d(TAG, "Token assigned successfully: $token")
                Result.success(loginResponse.result) // Return the `TokenHolder`
            } else {
                Log.e(TAG, "Token is null or empty")
                Result.failure(Exception("Token is null or empty"))
            }
        } else {
            val error = responseResult.exceptionOrNull()?.message ?: "Unknown error"
            Log.e(TAG, "Login failed: $error")
            Result.failure(Exception(error))
        }
    }
}
