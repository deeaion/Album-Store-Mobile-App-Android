package com.albumstore.todo.data.collection

import android.util.Log
import com.albumstore.core.data.remote.Api
import com.albumstore.todo.data.local.CollectionItemDao
import com.albumstore.todo.data.remote.collection.CollectionItemService
import com.albumstore.todo.data.remote.collection.CreateCollectionItemRequest

class CollectionItemRepository(
    private val collectionItemService: CollectionItemService,
    private val collectionItemDao: CollectionItemDao?
) {
    private fun getBearerToken() = "Bearer ${Api.tokenInterceptor.token}"

    /**
     * Fetches collection items from the API if online or from the local database if offline.
     */
    suspend fun fetchCollectionItems(online: Boolean): List<CollectionItem> {
        return try {
            if (online) {
                val response = collectionItemService.getAllCollectionItems(getBearerToken())
                Log.d("CollectionItemRepository", "Fetched items: ${response.records.size}")

                // Cache the fetched items locally
                collectionItemDao?.apply {
                    response.records.forEach { insertCollectionItem(it) }
                }
                response.records
            } else {
                collectionItemDao?.getAllCollectionItems() ?: emptyList()
            }
        } catch (e: Exception) {
            Log.e("CollectionItemRepository", "Error fetching items: ${e.localizedMessage}")
            throw e
        }
    }

    /**
     * Creates a new collection item via API and updates local storage.
     */
    suspend fun createCollectionItem(item: CollectionItem, online: Boolean) {
        try {
            if (online) {
                item.imageId="00000000-0000-0000-0000-000000000000"
                item.id="00000000-0000-0000-0000-000000000000"
                val requestPayload = CreateCollectionItemRequest.fromCollectionItem(item)
                Log.d("CollectionItemRepository", "Request Payload: $requestPayload")

                val response = collectionItemService.createCollectionItem(
                    authorization = getBearerToken(),
                    collectionItem = requestPayload
                )
                if (response.isValid) {
                    Log.d("CollectionItemRepository", "Item created successfully.")
                } else {
                    Log.e("CollectionItemRepository", "Validation errors: ${response.errors}")
                    response.errors?.forEach { (field, messages) ->
                        Log.e("CollectionItemRepository", "$field: ${messages.joinToString(", ")}")
                    }
                    throw Exception("Validation failed for the request.")
                }
//                fetchCollectionItems(true)
            }

            // Save to the local database for offline support
            collectionItemDao?.insertCollectionItem(item)
        } catch (e: Exception) {
            Log.e("CollectionItemRepository", "Error creating item: ${e.localizedMessage}")
            throw e
        }
    }






    /**
     * Deletes a collection item via API and removes it from local storage.
     */
    suspend fun deleteCollectionItem(id: String, online: Boolean) {
        try {
            if (online) {
                val response = collectionItemService.deleteCollectionItem(
                    authorization = getBearerToken(),
                    id = id
                )
                Log.d("CollectionItemRepository", "Deleted item response: ${response.message}")
            }
            collectionItemDao?.deleteCollectionItemById(id)
        } catch (e: Exception) {
            Log.e("CollectionItemRepository", "Error deleting item: ${e.localizedMessage}")
            throw e
        }
    }
}
