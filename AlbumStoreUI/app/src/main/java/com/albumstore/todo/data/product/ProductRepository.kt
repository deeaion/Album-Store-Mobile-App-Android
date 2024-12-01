package com.albumstore.todo.data.product

import android.util.Log
import com.albumstore.core.data.remote.Api
import com.albumstore.todo.data.local.ProductDao
import com.albumstore.todo.data.remote.FavoriteRequest
import com.albumstore.todo.data.remote.GetAllProductsFilter
import com.albumstore.todo.data.remote.ProductService
import com.albumstore.todo.data.remote.ProductWsClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import com.albumstore.todo.data.remote.ProductEvent
import com.albumstore.todo.data.remote.ProductRequest
import com.albumstore.todo.data.remote.toQueryMap
import com.albumstore.utils.Converters
import com.google.android.gms.nearby.connection.Payload

class ProductRepository(
    private val productService: ProductService,
    private val productDao: ProductDao,
    private val productWsClient: ProductWsClient
) {
    private val _productEvents = MutableSharedFlow<ProductEvent>()
    val productEvents = _productEvents.asSharedFlow()

    val products: Flow<List<Product>> = productDao.getAll()

    private fun getBearerToken() = "Bearer ${Api.tokenInterceptor.token}"

    suspend fun fetchProducts(filter: GetAllProductsFilter): List<Product> {
        val response = productService.getProducts(getBearerToken(), filter.toQueryMap())
        response.records.forEach { product ->
            Log.d("ProductRepository", "Product: $product") // Debug log for each product
        }
        productDao.deleteAll()
        productDao.insertAll(response.records)
        return response.records
    }

    suspend fun fetchProductDetail(id: String): ProductDetail {
        return productService.getProduct(getBearerToken(), id)
    }

    suspend fun saveProduct(productDetail: ProductDetail) {
        val updatedProduct = if (productDetail.id?.isNotBlank() == true) {
            productDetail.id.let {
                productService.updateProduct(
                    getBearerToken(),
                    ProductRequest(productDetail)
                )
            }
        } else {
            productService.createProduct(
                getBearerToken(),
                ProductRequest(productDetail.copy(id = null))
            )
        }

        productDao.insert(updatedProduct.toEntity())
    }

    suspend fun deleteProduct(id: String) {
        productService.deleteProduct(getBearerToken(), id)
        productDao.deleteById(id)
    }

    suspend fun deleteAll() {
        productDao.deleteAll()
    }

    suspend fun addProductToFavorites(productId: String) {
        productService.addProductToFavorites(getBearerToken(), FavoriteRequest(productId))
        updateFavoriteStatusLocally(productId, isFavorited = true)
    }

    suspend fun removeProductFromFavorites(productId: String) {
        productService.removeProductFromFavorites(getBearerToken(), FavoriteRequest(productId))
        updateFavoriteStatusLocally(productId, isFavorited = false)
    }

    private suspend fun updateFavoriteStatusLocally(productId: String, isFavorited: Boolean) {
        val product = productDao.getById(productId)
        if (product != null) {
            productDao.insert(product.copy(isFavorited = isFavorited))
        }
    }

    suspend fun openWebSocket(coroutineScope: CoroutineScope) {
        productWsClient.openSocket(
            coroutineScope = coroutineScope,
            onEvent = { productEvent ->
                productEvent?.let {
                    _productEvents.emit(it)
                    handleWebSocketEvent(it) // Handle events by refreshing the list
                }
            },
            onClosed = {
                Log.d("ProductRepository", "WebSocket closed.")
            },
            onFailure = { error ->
                Log.e("ProductRepository", "WebSocket error", error)
            }
        )
        productWsClient.authorize(getBearerToken())
    }

    fun closeWebSocket() {
        productWsClient.closeSocket()
    }

    private fun GetAllProductsFilter.toMap(): Map<String, String?> {
        return mapOf(
            "Skip" to skip?.toString(),
            "Take" to take?.toString(),
            "SortBy" to sortBy,
            "SortOrder" to sortOrder,
            "Search" to search,
            "ArtistName" to artistName,
            "Genre" to genre,
            "ArtistId" to artistId,
            "BandName" to bandName
        )
    }

    private fun ProductDetail.toEntity(): Product {
        return Product(
            id = this.id ?: "",
            name = this.name ?: "Unknown",
            price = this.price ?: 0.0,
            bandName = this.bandName ?: "Unknown Band",
            artistsName = this.artists?.joinToString(", "),
            image = this.baseImageUrl,
            baseImage = this.baseImage, // Directly assign ImageDto
            isFavorited = this.isFavorited ?: false
        )
    }

    suspend fun handleWebSocketEvent(event: ProductEvent) {
        when (event.type) {
            "ProductAdded" -> {
                Log.d("ProductRepository", "Product added event: ${event.productName}")
                refreshProductList() // Refresh the product list
            }

            "ProductDeleted" -> {
                Log.d("ProductRepository", "Product deleted event: ${event.productName}")
                refreshProductList() // Refresh the product list
            }

            else -> Log.w("ProductRepository", "Unhandled WebSocket event type: ${event.type}")
        }
    }

    private suspend fun refreshProductList() {
        try {
            val filter = GetAllProductsFilter(skip = 0, take = 50) // Customize filter as needed
            fetchProducts(filter)
            Log.d("ProductRepository", "Product list refreshed successfully.")
        } catch (e: Exception) {
            Log.e("ProductRepository", "Failed to refresh product list", e)
        }
    }
}
