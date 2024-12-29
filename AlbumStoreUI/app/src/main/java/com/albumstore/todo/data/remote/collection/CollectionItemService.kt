package com.albumstore.todo.data.remote.collection

import com.albumstore.todo.data.collection.CollectionItem
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

interface CollectionItemService {

    @GET("/api/collection")
    suspend fun getAllCollectionItems(
        @Header("Authorization") authorization: String
    ): GetAllCollectionItemsResponse

    @GET("/api/collection/{id}")
    suspend fun getCollectionItemById(
        @Header("Authorization") authorization: String,
        @Path("id") id: String
    ): CollectionItem

    @POST("/api/collection")
    suspend fun createCollectionItem(
        @Header("Authorization") authorization: String,
        @Body collectionItem: CreateCollectionItemRequest
    ): CreateCollectionResponse

    @DELETE("/api/collection/{id}")
    suspend fun deleteCollectionItem(
        @Header("Authorization") authorization: String,
        @Path("id") id: String
    ): DeleteCollectionResponse
}

// Request and Response Models
data class GetAllCollectionItemsResponse(
    val records: List<CollectionItem>,
    val totalNumberOfRecords: Int
)


data class CreateCollectionItemRequest(
    @SerializedName("collectionItem")
    val collectionItem: Map<String, Any?>
) {
    companion object {
        fun fromCollectionItem(item: CollectionItem): CreateCollectionItemRequest {
            val collectionItemMap = mutableMapOf<String, Any?>()

            // Dynamically add fields only if they are not null or empty
            collectionItemMap["id"] = item.id
            item.productId?.let { collectionItemMap["productId"] = it }
            item.imageId?.takeIf { it.isNotEmpty() }?.let { collectionItemMap["imageId"] = it }
            collectionItemMap["title"] = item.title
            collectionItemMap["artist"] = item.artist

            item.image?.let {
                collectionItemMap["image"] = mapOf(
                    "imageBase64" to it.imageBase64,
                    "contentType" to it.contentType,
                    "fileName" to it.fileName
                )
            }

            return CreateCollectionItemRequest(collectionItem = collectionItemMap)
        }

    }
}


data class CollectionItemPayload(
    val id: String,
    val productId: String?,
    val imageId: String?,
    val title: String,
    val artist: String,
    val image: ImagePayload?
)

data class ImagePayload(
    val imageBase64: String?,
    val contentType: String?,
    val fileName: String?
)





data class CreateCollectionResponse(
    val errors: Map<String, List<String>>?, // Error messages mapped to fields
    val isValid: Boolean // Indicates if the request was valid
)

data class DeleteCollectionResponse(
    val message: String
)
