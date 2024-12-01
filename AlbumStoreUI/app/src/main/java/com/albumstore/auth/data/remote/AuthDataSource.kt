package com.albumstore.auth.data.remote
import android.util.Log
import com.albumstore.auth.TAG
import com.albumstore.core.data.remote.Api
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

class AuthDataSource () {
    interface AuthService {
        @Headers("Content-Type: application/json")
        @POST("/api/auth/login")
        suspend fun login(@Body user: User): Response<LoginResponse>
    }
    private val authService: AuthService = Api.retrofit.create(AuthService::class.java)

    suspend fun login(user: User): Result<LoginResponse> {
        return try {
            val response = authService.login(user)
            Log.d(TAG, "Raw response: ${response.raw()}")
            Log.d(TAG, "Response body: ${response.body()}")

            val body = response.body()
            if (response.isSuccessful && body != null) {
                Result.success(body)
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "Login failed: $errorBody")
                Result.failure(Exception("Login failed: $errorBody"))
            }
        } catch (e: Exception) {
            Log.w(TAG, "login failed", e)
            Result.failure(e)
        }
    }


}