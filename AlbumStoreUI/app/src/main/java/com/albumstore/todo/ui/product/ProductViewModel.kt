package com.albumstore.todo.ui.product

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.albumstore.core.TAG
import com.albumstore.todo.data.product.Product
import com.albumstore.todo.data.product.ProductDetail
import com.albumstore.todo.data.product.ProductRepository
import com.albumstore.todo.data.remote.GetAllProductsFilter
import com.albumstore.todo.data.remote.ProductEvent
import com.albumstore.utils.WebSocketManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import retrofit2.HttpException

data class ProductUiState(
    val products: List<Product> = emptyList(),
    val productDetail: ProductDetail? = null,
    val fetching: Boolean = false,
    val fetchingError: String? = null,
    val saving: Boolean = false,
    val savingError: String? = null,
    val notification: String? = null, // Add notification for UI
    val totalNumberOfRecords: Int = 0
)

class ProductViewModel(
    private val productRepository: ProductRepository,
    private val webSocketManager: WebSocketManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductUiState())
    val uiState: StateFlow<ProductUiState> = _uiState

    private var skip = 0
    private val take = 10

    init {
        loadProducts(reset = true)
        subscribeToWebSocketEvents()
    }

    private fun subscribeToWebSocketEvents() {
        viewModelScope.launch {
            webSocketManager.startConnection(
                viewModelScope,
                ""
            )
            webSocketManager.socketEventsFlow.collectLatest { event ->
                handleWebSocketEvent(event)
            }
        }
    }

    private fun handleWebSocketEvent(event: ProductEvent) {
        when (event.type) {
            "ProductAdded" -> handleProductAdded(event)
            "ProductDeleted" -> handleProductDeleted(event)
            else -> Log.w(TAG, "Unknown WebSocket event type: ${event.type}")
        }
    }

    private fun handleProductAdded(event: ProductEvent) {

        _uiState.value = _uiState.value.copy(
            notification = "Product Added: ${event.productName}"
        )
        loadProducts(reset = true)
        Log.d(TAG, "Product added via WebSocket: ${event.productName}")
    }

    private fun handleProductDeleted(event: ProductEvent) {
        _uiState.value = _uiState.value.copy(
            notification = "Product Deleted: ${event.productName}"
        )
        loadProducts(reset = true)
        Log.d(TAG, "Product deleted via WebSocket: ${event.productName}")
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

                val updatedProducts = if (reset) {
                    fetchedProducts
                } else {
                    _uiState.value.products + fetchedProducts
                }

                _uiState.value = _uiState.value.copy(
                    products = updatedProducts,
                    totalNumberOfRecords = updatedProducts.size,
                    fetching = false
                )
                skip += take
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load products", e)
                _uiState.value = _uiState.value.copy(fetching = false, fetchingError = parseError(e))
            }
        }
    }

    fun loadProductDetail(productId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(fetching = true)
            try {
                val productDetail = productRepository.fetchProductDetail(productId)
                _uiState.value = _uiState.value.copy(productDetail = productDetail, fetching = false)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load product detail", e)
                _uiState.value = _uiState.value.copy(fetching = false, fetchingError = parseError(e))
            }
        }
    }

    fun saveProduct(productDetail: ProductDetail) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(saving = true)
            try {
                productRepository.saveProduct(productDetail)
                _uiState.value = _uiState.value.copy(saving = false)
                Log.d(TAG, "Product saved successfully: $productDetail")
            } catch (e: Exception) {
                val errorDetails = if (e is HttpException) {
                    e.response()?.errorBody()?.string()
                } else null
                Log.e(TAG, "Failed to save product. Server response: $errorDetails", e)
                _uiState.value = _uiState.value.copy(saving = false, savingError = parseError(e))
            }
        }
    }

    private fun parseError(exception: Exception): String {
        return when (exception) {
            is HttpException -> {
                val errorBody = exception.response()?.errorBody()?.string()
                "HTTP ${exception.code()}: ${errorBody ?: "Unknown server error"}"
            }
            else -> exception.localizedMessage ?: "Unknown error"
        }
    }

    companion object {
        fun Factory(productRepository: ProductRepository, webSocketManager: WebSocketManager) =
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(ProductViewModel::class.java)) {
                        @Suppress("UNCHECKED_CAST")
                        return ProductViewModel(productRepository, webSocketManager) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
    }
}
