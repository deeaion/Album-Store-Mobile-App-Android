package com.albumstore.todo.ui.products

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.albumstore.core.data.remote.UserPreferencesRepository
import com.albumstore.todo.data.product.Product
import com.albumstore.todo.data.product.ProductRepository
import com.albumstore.todo.data.remote.GetAllProductsFilter
import com.albumstore.todo.data.remote.ProductEvent
import com.albumstore.todo.data.tasks.PendingTask
import com.albumstore.todo.data.tasks.TaskRepository
import com.albumstore.utils.notifications.showSimpleNotification
import com.albumstore.utils.sockets.WebSocketManager
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
    private val webSocketManager: WebSocketManager,
    private val taskRepository: TaskRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductsUiState())
    val uiState: StateFlow<ProductsUiState> = _uiState
    //context

    private var skip = 0
    private val take = 10

    init {
        viewModelScope.launch {
            fetchUserRole()
            loadProducts(reset = true)
            syncPendingTasks()
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

    fun collectWebSocketEvents() {
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
                _uiState.value = _uiState.value.copy(
                    notification = Notification(
                        title = "Product Added",
                        message = "${event.productName} has been added."
                    )
                )
                loadProducts(reset = true)
            }
            "ProductDeleted" -> {
                Log.d("ProductsViewModel", "Product deleted via WebSocket: ${event.productName}")
                _uiState.value = _uiState.value.copy(
                    notification = Notification(
                        title = "Product Removed",
                        message = "${event.productName} has been removed."
                    )
                )
                loadProducts(reset = true)
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
            val updatedProducts = _uiState.value.products.map { product ->
                if (product.id == productId) {
                    val isFavorited = !product.isFavorited

                    // Update the UI immediately
                    product.copy(isFavorited = isFavorited)
                } else product
            }

            // Update the UI state before making the network call
            _uiState.value = _uiState.value.copy(products = updatedProducts)

            try {
                // Attempt network call
                val isFavorited = updatedProducts.first { it.id == productId }.isFavorited

                if (isFavorited) {
                    productRepository.addProductToFavorites(productId)
                } else {
                    productRepository.removeProductFromFavorites(productId)
                }

            } catch (e: Exception) {
                Log.e("ProductsViewModel", "Failed to toggle favorite", e)

                // Save task for offline sync if network fails
                taskRepository.addPendingTask(
                    PendingTask(
                        taskType = "TOGGLE_FAVORITE",
                        productId = productId,
                        isFavorited = updatedProducts.first { it.id == productId }.isFavorited
                    )
                )
            }
        }
    }

    private fun syncPendingTasks() {
        viewModelScope.launch {
            try {
                taskRepository.processPendingTasks()
                Log.d("ProductsViewModel", "Pending tasks synced successfully.")
            } catch (e: Exception) {
                Log.e("ProductsViewModel", "Failed to sync pending tasks", e)
            }
        }
    }
    companion object {
        fun Factory(
            productRepository: ProductRepository,
            userPreferencesRepository: UserPreferencesRepository,
            webSocketManager: WebSocketManager,
            taskRepository: TaskRepository
        ) = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(ProductsViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    return ProductsViewModel(productRepository, userPreferencesRepository, webSocketManager,taskRepository) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
}
