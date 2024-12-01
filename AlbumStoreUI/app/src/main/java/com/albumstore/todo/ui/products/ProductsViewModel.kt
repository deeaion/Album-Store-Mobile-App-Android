package com.albumstore.todo.ui.products

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.albumstore.core.data.remote.UserPreferencesRepository
import com.albumstore.todo.data.product.Product
import com.albumstore.todo.data.product.ProductDetail
import com.albumstore.todo.data.product.ProductRepository
import com.albumstore.todo.data.remote.GetAllProductsFilter
import com.albumstore.todo.data.remote.ProductEvent
import com.albumstore.todo.ui.product.ProductViewModel
import com.albumstore.utils.WebSocketManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class Notification(
    val title: String,
    val message: String
)

data class ProductsUiState(
    val products: List<Product> = emptyList(),
    val fetching: Boolean = false,
    val fetchingError: String? = null,
    val userRole: String = "",
    val totalNumberOfRecords: Int = 0,
    val notification: Notification? = null
)

class ProductsViewModel(
    private val productRepository: ProductRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val webSocketManager: WebSocketManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductsUiState())
    val uiState: StateFlow<ProductsUiState> = _uiState

    private var skip = 0
    private val take = 10

    init {
        viewModelScope.launch {
            fetchUserRole()
            loadProducts(reset = true)
            collectWebSocketEvents()
        }
    }

    private suspend fun fetchUserRole() {
        try {
            val roles = userPreferencesRepository.userPreferencesStream.first().roles
            Log.d("ProductsViewModel", "Fetched roles: $roles")
            _uiState.value = _uiState.value.copy(userRole = roles)
        } catch (e: Exception) {
            Log.e("ProductsViewModel", "Failed to fetch user role", e)
        }
    }

    private fun collectWebSocketEvents() {
        viewModelScope.launch {
            webSocketManager.socketEventsFlow.collect { event ->
                handleWebSocketEvent(event)
            }
        }
    }




    private fun handleWebSocketEvent(event: ProductEvent) {
        when (event.type) {
            "ProductAdded" -> {
                Log.d("ProductsViewModel", "Product added via WebSocket: ${event.productName}")
                showNotification("Product Added", "${event.productName} has been added.")
                loadProducts(reset = true) // Refresh the list
            }
            "ProductDeleted" -> {
                Log.d("ProductsViewModel", "Product deleted via WebSocket: ${event.productName}")
                showNotification("Product Removed", "${event.productName} has been removed.")
                loadProducts(reset = true) // Refresh the list
            }
            else -> Log.w("ProductsViewModel", "Unknown WebSocket event type: ${event.type}")
        }
    }

    private fun showNotification(title: String, message: String) {
        _uiState.value = _uiState.value.copy(notification = Notification(title, message))
    }

    fun clearNotification() {
        _uiState.value = _uiState.value.copy(notification = null)
    }

    fun loadProducts(reset: Boolean = false) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(fetching = true)
            try {
                if (reset) skip = 0
                val filter = GetAllProductsFilter(skip = skip, take = take)
                val fetchedProducts = productRepository.fetchProducts(filter)

                val updatedProducts = if (reset) fetchedProducts else _uiState.value.products + fetchedProducts

                _uiState.value = _uiState.value.copy(
                    products = updatedProducts,
                    totalNumberOfRecords = updatedProducts.size,
                    fetching = false
                )
                skip += take
            } catch (e: Exception) {
                Log.e("ProductsViewModel", "Failed to load products", e)
                _uiState.value = _uiState.value.copy(fetching = false, fetchingError = e.localizedMessage)
            }
        }
    }

    fun reloadProducts() {
        loadProducts(reset = true)
    }

    fun toggleFavorite(productId: String) {
        viewModelScope.launch {
            try {
                val updatedProducts = _uiState.value.products.map { product ->
                    if (product.id == productId) {
                        val isFavorited = !product.isFavorited
                        if (isFavorited) {
                            productRepository.addProductToFavorites(productId)
                        } else {
                            productRepository.removeProductFromFavorites(productId)
                        }
                        product.copy(isFavorited = isFavorited)
                    } else product
                }
                _uiState.value = _uiState.value.copy(products = updatedProducts)
            } catch (e: Exception) {
                Log.e("ProductsViewModel", "Failed to toggle favorite", e)
            }
        }
    }

    companion object {
        fun Factory(
            productRepository: ProductRepository,
            userPreferencesRepository: UserPreferencesRepository,
            webSocketManager: WebSocketManager
        ) = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(ProductsViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    return ProductsViewModel(productRepository, userPreferencesRepository, webSocketManager) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
}
