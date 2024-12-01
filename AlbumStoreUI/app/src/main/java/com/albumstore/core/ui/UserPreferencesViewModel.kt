package com.albumstore.core.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.albumstore.MyApplication
import com.albumstore.core.data.UserPreferences
import com.albumstore.core.data.remote.Api
import com.albumstore.core.data.remote.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class UserPreferencesViewModel(private val userPreferencesRepository: UserPreferencesRepository) :
    ViewModel() {

    // Expose user preferences as a Flow
    val uiState: Flow<UserPreferences> = userPreferencesRepository.userPreferencesStream

    init {
        Log.d(TAG, "UserPreferencesViewModel initialized")

        // Update TokenInterceptor whenever the token changes
        viewModelScope.launch {
            uiState.collect { userPreferences ->
                Api.tokenInterceptor.token = userPreferences.token
                Log.d(TAG, "TokenInterceptor updated with token: ${userPreferences.token}")
            }
        }
    }

    /**
     * Checks if the current user is an admin.
     * This is a suspend function to be called in coroutine context.
     */
    suspend fun isAdmin(): Boolean {
        val userPreferences = uiState.first()
        return userPreferences.roles.contains("Admin", ignoreCase = true)
    }

    /**
     * Saves user preferences to the repository.
     */
    fun save(userPreferences: UserPreferences) {
        viewModelScope.launch {
            userPreferencesRepository.save(userPreferences)
            Log.d(TAG, "User preferences saved: $userPreferences")
        }
    }

    /**
     * Clears all user preferences and resets the TokenInterceptor.
     */
    fun clearPreferences() {
        viewModelScope.launch {
            userPreferencesRepository.clear()
            Api.tokenInterceptor.token = null
            Log.d(TAG, "User preferences cleared and TokenInterceptor reset")
        }
    }

    companion object {
        private const val TAG = "UserPreferencesViewModel"

        // Factory for creating the ViewModel
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app =
                    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MyApplication)
                UserPreferencesViewModel(app.container.userPreferencesRepository)
            }
        }
    }
}
