package com.albumstore.core.data.remote

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response

class TokenInterceptor : Interceptor {
    var token: String? = null

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // Log the URL and token state for debugging
        Log.d("TokenInterceptor", "Request URL: ${originalRequest.url}")
        Log.d("TokenInterceptor", "Current token: ${token ?: "No token provided"}")

        // If the token is null, proceed without the Authorization header
        if (token.isNullOrEmpty()) {
            Log.w("TokenInterceptor", "No token available, proceeding without Authorization header")
            return chain.proceed(originalRequest)
        }

        // Add Authorization header with the token
        val modifiedRequest = originalRequest.newBuilder()
            .addHeader("Authorization", "Bearer $token")
            .build()

        Log.d("TokenInterceptor", "Authorization header added to request")

        // Proceed with the modified request
        val response = chain.proceed(modifiedRequest)

        // Log the response status code for debugging
        Log.d("TokenInterceptor", "Response status code: ${response.code}")

        return response
    }
}
