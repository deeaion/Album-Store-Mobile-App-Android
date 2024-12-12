package com.albumstore.utils.sockets

import android.util.Log
import com.albumstore.todo.data.remote.ProductEvent
import com.microsoft.signalr.HubConnection
import com.microsoft.signalr.HubConnectionBuilder
import com.microsoft.signalr.HubConnectionState
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import io.reactivex.rxjava3.core.Single
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import okhttp3.internal.wait

class WebSocketManager(
    private val url: String,
    private val token: String,
    private var userId: String
) {

    var hubConnection: HubConnection? = null
    private val eventChannel = Channel<ProductEvent>(Channel.BUFFERED)
    val socketEventsFlow = eventChannel.receiveAsFlow()

    private val maxReconnectAttempts = 10
    private var reconnectAttempts = 0
    private val reconnectDelay = 6000L // 5 seconds

    fun startConnection(coroutineScope: CoroutineScope, userId: String) {
        if(userId.isNotEmpty()){
            this.userId = userId
        }
        if (hubConnection?.connectionState == HubConnectionState.CONNECTED) {
            Log.d(TAG, "WebSocket already connected.")
            return
        }

        hubConnection = HubConnectionBuilder.create("$url?userId=${this.userId}")
            .withAccessTokenProvider(Single.just(token))
            .build()

        hubConnection?.on("ReceiveMessage", { message ->
            try {
                val event = parseEvent(message)
                coroutineScope.launch {
                    eventChannel.send(event)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to parse WebSocket message", e)
            }
        }, String::class.java)

        hubConnection?.onClosed {
            Log.d(TAG, "WebSocket connection closed.")
            reconnectAttempts = 0
            attemptReconnect(coroutineScope)
        }

        coroutineScope.launch(Dispatchers.IO) {
            try {
                hubConnection?.start()?.blockingAwait()
                reconnectAttempts = 0
                Log.d(TAG, "WebSocket connection established.")
            } catch (e: Exception) {
                Log.e(TAG, "WebSocket connection failed to start", e)
                attemptReconnect(coroutineScope)
            }
        }
    }

    private fun attemptReconnect(coroutineScope: CoroutineScope) {
        if (reconnectAttempts >= maxReconnectAttempts) {
            Log.w(TAG, "Max reconnect attempts reached. Waiting before retrying...")
            coroutineScope.launch {
                delay(1000) // Wait 60 seconds before resetting attempts
                reconnectAttempts = 0
                startConnection(coroutineScope, userId)
            }
            return
        }

        reconnectAttempts++
        coroutineScope.launch {
            delay(reconnectDelay)
            Log.d(TAG, "Reconnecting... (Attempt $reconnectAttempts)")
            startConnection(coroutineScope, userId)
        }
    }
    fun monitorConnection(coroutineScope: CoroutineScope) {
        coroutineScope.launch {
            while (true) {
                delay(10000) // Check every 10 seconds
                if (hubConnection?.connectionState != HubConnectionState.CONNECTED) {
                    Log.d(TAG, "Connection lost. Retrying...")
                    startConnection(coroutineScope, userId)
                }
            }
        }
    }


    private fun parseEvent(json: String): ProductEvent {
        Log.d(TAG, "Parsing WebSocket message: $json")
        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        val adapter = moshi.adapter(ProductEvent::class.java)
        return adapter.fromJson(json) ?: throw IllegalArgumentException("Invalid WebSocket message")
    }

    fun stopConnection() {
        hubConnection?.stop()
        hubConnection = null
        Log.d(TAG, "WebSocket connection stopped.")
    }

    companion object {
        private const val TAG = "WebSocketManager"
    }
}
