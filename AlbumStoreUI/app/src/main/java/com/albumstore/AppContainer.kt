package com.albumstore

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.preferencesDataStore
import com.albumstore.auth.data.remote.AuthDataSource
import com.albumstore.auth.data.remote.AuthRepository
import com.albumstore.core.TAG
import com.albumstore.core.data.remote.Api
import com.albumstore.core.data.remote.UserPreferencesRepository
import com.albumstore.todo.data.band.BandRepository
import com.albumstore.todo.data.product.ProductRepository
import com.albumstore.todo.data.remote.ProductService
import com.albumstore.todo.data.remote.ProductWsClient
import com.albumstore.todo.data.remote.band.BandService
import com.albumstore.utils.WebSocketManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

val Context.userPreferencesDataStore by preferencesDataStore(
    name = "user_preferences"
)

class AppContainer(val context: Context) {
    init {
        Log.d(TAG, "AppContainer initialized")
    }

    // Retrofit service for product-related API calls
    private val productService: ProductService = Api.retrofit.create(ProductService::class.java)

    // WebSocket client for real-time product updates
    public val productWsClient: ProductWsClient = ProductWsClient(Api.okHttpClient)

    // WebSocket Manager for handling events
    val webSocketManager: WebSocketManager by lazy {
        WebSocketManager(
            url = Api.wsUrl,
            token = Api.tokenInterceptor.token ?: "",
            userId = "",

        )
    }

    // Lazy initialization of the database instance
    private val database: MyAppDatabase by lazy { MyAppDatabase.getDatabase(context) }

    // Repository for product-related operations
    val productRepository: ProductRepository by lazy {
        ProductRepository(productService, database.productDao(), productWsClient)
    }

    // Repository for authentication operations
    val authRepository: AuthRepository by lazy {
        AuthRepository(AuthDataSource())
    }

    val bandService: BandService = Api.retrofit.create(BandService::class.java)

    // Repository for band-related operations
    val bandRepository: BandRepository by lazy {
        BandRepository(bandService, database.bandDao())
    }

    // Repository for user preferences management
    val userPreferencesRepository: UserPreferencesRepository by lazy {
        UserPreferencesRepository(context.userPreferencesDataStore)
    }

    // Manage WebSocket lifecycle
    fun initializeWebSocket() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                webSocketManager.startConnection(this,userPreferencesRepository.userPreferencesStream.first().userId)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize WebSocket", e)
            }
        }
    }

    fun closeWebSocket() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                webSocketManager.stopConnection()
                Log.d(TAG, "WebSocket closed from AppContainer")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to close WebSocket", e)
            }
        }
    }
}
