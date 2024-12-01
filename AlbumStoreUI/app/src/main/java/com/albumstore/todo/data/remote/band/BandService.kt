package com.albumstore.todo.data.remote.band

import com.albumstore.todo.data.band.Band
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface BandService {

    @GET("/api/band")
    suspend fun getBands(
        @Header("Authorization") authorization: String
    ): GetAllBandsResponse

    @POST("/api/band/favorite")
    suspend fun addBandToFavorites(
        @Header("Authorization") authorization: String,
        @Body favoriteRequest: FavoriteRequest
    ): FavoriteResponse
}

// Response model for getting all bands
data class GetAllBandsResponse(
    val records: List<Band>,
    val totalNumberOfRecords: Int
)

// Request model for adding a band to favorites
data class FavoriteRequest(
    val bandId: String
)

// Response model for favorite actions
data class FavoriteResponse(
    val message: String
)
