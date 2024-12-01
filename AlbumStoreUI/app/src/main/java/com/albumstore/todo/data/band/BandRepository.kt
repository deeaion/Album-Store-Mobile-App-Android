package com.albumstore.todo.data.band

import android.util.Log
import com.albumstore.core.data.remote.Api
import com.albumstore.todo.data.local.BandDao
import com.albumstore.todo.data.remote.band.BandService
import com.albumstore.todo.data.remote.band.FavoriteRequest

class BandRepository(
    private val bandService: BandService,
    private val bandDao: BandDao?
) {
    private fun getBearerToken() = "Bearer ${Api.tokenInterceptor.token}"

    /**
     * Fetches bands from the API if online or from the local database if offline.
     */
    suspend fun fetchBands(online: Boolean): List<Band> {
        return if (online) {
            val response = bandService.getBands(getBearerToken())
            Log.d("BandRepository", "Fetched bands from API: ${response.records}")

            // Cache bands in the local database (optional)
            bandDao?.deleteAll()
            response.records
        } else {
            // Fetch bands from local database if offline
            bandDao?.getAllBands() ?: emptyList()
        }
    }

    /**
     * Adds a band to the user's favorites.
     */


    /**
     * Removes a band from the user's favorites.
     */


    /**
     * Updates the favorite status of a band in the local database.
     */


}
