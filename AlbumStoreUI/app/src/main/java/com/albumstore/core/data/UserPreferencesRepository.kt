package com.albumstore.core.data.remote

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.albumstore.core.data.UserPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class UserPreferencesRepository(private val dataStore: DataStore<Preferences>) {

    private object PreferencesKeys {
        val username = stringPreferencesKey("username")
        val token = stringPreferencesKey("token")
        val roles = stringPreferencesKey("roles")
        val userId = stringPreferencesKey("userId")
    }

    init {
        Log.d(TAG, "UserPreferencesRepository initialized")
    }

    // Expose the user preferences as a Flow
    val userPreferencesStream: Flow<UserPreferences> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                Log.e(TAG, "IOException when accessing DataStore, emitting emptyPreferences", exception)
                emit(emptyPreferences())
            } else {
                Log.e(TAG, "Unexpected exception when accessing DataStore", exception)
                throw exception
            }
        }
        .map { preferences ->
            mapUserPreferences(preferences).also {
                Log.d(TAG, "Mapped preferences: $it")
            }
        }

    // Save user preferences
    suspend fun save(userPreferences: UserPreferences) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.username] = userPreferences.username
            preferences[PreferencesKeys.token] = userPreferences.token
            preferences[PreferencesKeys.roles] = userPreferences.roles
            preferences[PreferencesKeys.userId] = userPreferences.userId
            Log.d(TAG, "Preferences saved: $userPreferences")
        }
    }

    // Clear all user preferences
    suspend fun clear() {
        dataStore.edit { it.clear() }
        Log.d(TAG, "User preferences cleared")
    }

    // Map preferences to UserPreferences object
    private fun mapUserPreferences(preferences: Preferences): UserPreferences {
        return UserPreferences(
            username = preferences[PreferencesKeys.username] ?: "",
            token = preferences[PreferencesKeys.token] ?: "",
            roles = preferences[PreferencesKeys.roles] ?: "",
            userId = preferences[PreferencesKeys.userId] ?: ""
        )
    }
    // Add a method to get user preferences
    suspend fun getUserPreferences(): UserPreferences {
        return userPreferencesStream
            .map { it }
            .first()
    }

    companion object {
        private const val TAG = "UserPreferencesRepo"
    }
}
