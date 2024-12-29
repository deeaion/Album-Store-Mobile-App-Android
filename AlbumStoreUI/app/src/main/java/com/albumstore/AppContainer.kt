package com.albumstore

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.preferencesDataStore
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.albumstore.auth.data.remote.AuthDataSource
import com.albumstore.auth.data.remote.AuthRepository
import com.albumstore.core.TAG
import com.albumstore.core.data.remote.Api
import com.albumstore.core.data.remote.UserPreferencesRepository
import com.albumstore.todo.data.band.BandRepository
import com.albumstore.todo.data.collection.CollectionItemRepository
import com.albumstore.todo.data.product.ProductRepository
import com.albumstore.todo.data.remote.ProductService
import com.albumstore.todo.data.remote.ProductWsClient
import com.albumstore.todo.data.remote.band.BandService
import com.albumstore.todo.data.remote.collection.CollectionItemService
import com.albumstore.todo.data.tasks.TaskRepository
import com.albumstore.utils.conectivitymanager.ConnectivityManagerNetworkMonitor
import com.albumstore.utils.sockets.WebSocketManager
import com.albumstore.utils.workermanager.ProductSyncWorker
import com.microsoft.signalr.HubConnectionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

val Context.userPreferencesDataStore by preferencesDataStore(
    name = "user_preferences"
)

class AppContainer(val context: Context) {
    init {
        Log.d(TAG, "AppContainer initialized")
        scheduleProductReminderWork(context)

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
    //conectivity manager
    val connectivityManager = ConnectivityManagerNetworkMonitor(context)
    val collectionItemService : CollectionItemService= Api.retrofit.create(CollectionItemService::class.java)

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

    val taskRepository: TaskRepository by lazy {
        TaskRepository(context,database.taskDao(), productService)
    }

    val collectionRepository: CollectionItemRepository by lazy {
        CollectionItemRepository( collectionItemService, database.collectionItemDao())
    }
    fun scheduleProductReminderWork(context: Context) {
        val workRequest = PeriodicWorkRequestBuilder<ProductSyncWorker>(
            3, TimeUnit.MINUTES
        ).build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "ProductSyncWorker",
            ExistingPeriodicWorkPolicy.REPLACE,
            workRequest
        )
    }

    // Manage WebSocket lifecycle
    fun initializeWebSocket() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val userId = userPreferencesRepository.userPreferencesStream.first().userId
                webSocketManager.startConnection(this, userId)
                monitorWebSocketConnection() // Start monitoring the WebSocket connection
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize WebSocket", e)
            }
        }
    }
    private var monitoringJob: Job? = null
    fun monitorWebSocketConnection(): Job {
        return CoroutineScope(Dispatchers.IO).launch {
            while (true) {
                try {
                    if (webSocketManager.hubConnection?.connectionState != HubConnectionState.CONNECTED) {
                        Log.d(TAG, "WebSocket disconnected. Attempting to reconnect...")
                        webSocketManager.startConnection(this, userPreferencesRepository.userPreferencesStream.first().userId)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to monitor WebSocket connection", e)
                }
                delay(10000) // Check every 10 seconds
            }
        }
    }


    fun closeWebSocket() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                webSocketManager.stopConnection()
                monitoringJob?.cancel() // Cancel the monitoring job
                Log.d(TAG, "WebSocket closed from AppContainer")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to close WebSocket", e)
            }
        }
    }
}
