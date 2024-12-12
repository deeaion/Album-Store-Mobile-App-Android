package com.albumstore.auth

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.albumstore.MyApplication
import com.albumstore.auth.data.remote.AuthRepository
import com.albumstore.core.data.UserPreferences
import com.albumstore.core.data.remote.UserPreferencesRepository
import kotlinx.coroutines.launch

data class LoginUiState(
    val isAuthenticating: Boolean = false,
    val authenticationError: Throwable? = null,
    val authenticationCompleted: Boolean = false,
    val token: String = ""
)

class LoginViewModel(
    private val authRepository: AuthRepository,
    private val userPrefRepository: UserPreferencesRepository
) : ViewModel() {

    var uiState: LoginUiState by mutableStateOf(LoginUiState())

    init {
        Log.d(TAG, "LoginViewModel initialized")
    }

    fun login(username: String, password: String) {
        viewModelScope.launch {
            Log.d(TAG, "Starting login for username: $username")
            uiState = uiState.copy(isAuthenticating = true, authenticationError = null)

            val result = authRepository.login(username, password)

            if (result.isSuccess) {
                val tokenHolder = result.getOrNull()
                val token = tokenHolder?.token ?: ""
                val roles = tokenHolder?.user?.roles?.joinToString(",") ?: ""

                Log.d(TAG, "Login successful. Extracted token: $token")
                Log.d(TAG, "Extracted roles: $roles")

                // Save user preferences
                userPrefRepository.save(
                    UserPreferences(
                        username = username,
                        token = token,
                        roles = roles,
                        userId = tokenHolder?.user?.id ?: ""
                    )
                )
                Log.d(TAG, "User preferences saved")
                uiState = uiState.copy(
                    isAuthenticating = false,
                    authenticationCompleted = true,
                    token = token
                )
            } else {
                val errorMessage = result.exceptionOrNull()?.message ?: "Unknown error"
                Log.e(TAG, "Login failed: $errorMessage")

                // Check if token already exists
                val savedPreferences = userPrefRepository.getUserPreferences()
                if (savedPreferences.token.isNotEmpty()) {
                    Log.d(TAG, "Found saved token. Proceeding with stored token.")
                    uiState = uiState.copy(
                        isAuthenticating = false,
                        authenticationCompleted = true,
                        token = savedPreferences.token
                    )
                } else {
                    Log.e(TAG, "No saved token found.")
                    uiState = uiState.copy(
                        isAuthenticating = false,
                        authenticationError = result.exceptionOrNull()
                    )
                }
            }
        }
    }


    companion object {
        private const val TAG = "LoginViewModel"

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MyApplication)
                LoginViewModel(
                    app.container.authRepository,
                    app.container.userPreferencesRepository
                )
            }
        }
    }
}
