package com.albumstore.todo.data.remote

import com.albumstore.todo.data.product.ProductDetail
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.*

interface ProductService {

    @GET("/api/product")
    suspend fun getProducts(
        @Header("Authorization") authorization: String,
        @QueryMap filter: Map<String, String?>
    ): GetAllProductsResponse

    @GET("/api/product/{id}")
    suspend fun getProduct(
        @Header("Authorization") authorization: String,
        @Path("id") id: String
    ): ProductDetail

    @POST("/api/product")
    suspend fun createProduct(
        @Header("Authorization") authorization: String,
        @Body productRequest: ProductRequest
    ): ProductDetail

    @PUT("/api/product")
    suspend fun updateProduct(
        @Header("Authorization") authorization: String,
        @Body product:ProductRequest
    ): ProductDetail

    @DELETE("/api/product/{id}")
    suspend fun deleteProduct(
        @Header("Authorization") authorization: String,
        @Path("id") id: String
    )

    // Add product to favorites
    @POST("/api/product/favorite")
    suspend fun addProductToFavorites(
        @Header("Authorization") authorization: String,
        @Body favoriteRequest: FavoriteRequest
    )

    // Remove product from favorites
    @DELETE("/api/product/favorite")
    suspend fun removeProductFromFavorites(
        @Header("Authorization") authorization: String,
        @Body favoriteRequest: FavoriteRequest
    )
}

// Data class for request body in favorite actions
data class FavoriteRequest(
    val productId: String
)