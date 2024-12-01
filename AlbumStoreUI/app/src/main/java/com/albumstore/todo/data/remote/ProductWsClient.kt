package com.albumstore.todo.data.remote

import android.util.Log
import com.albumstore.core.TAG
import com.albumstore.core.data.remote.Api
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import java.util.concurrent.atomic.AtomicBoolean

class ProductWsClient(private val okHttpClient: OkHttpClient) {

    private var webSocket: WebSocket? = null
    private val isConnected = AtomicBoolean(false)

    /**
     * Open a WebSocket connection.
     *
     * @param coroutineScope Coroutine scope for launching events.
     * @param onEvent Callback to handle incoming WebSocket messages.
     * @param onClosed Callback when the WebSocket is closed.
     * @param onFailure Callback when the WebSocket encounters an error.
     */
    fun openSocket(
        coroutineScope: CoroutineScope,
        onEvent: suspend (productEvent: ProductEvent?) -> Unit,
        onClosed: () -> Unit,
        onFailure: (Throwable) -> Unit
    ): WebSocket? { // Return the WebSocket instance
        Log.d(TAG, "Attempting to open WebSocket connection.")
        val request = Request.Builder().url(Api.wsUrl).build()
        webSocket = okHttpClient.newWebSocket(
            request,
            ProductWebSocketListener(
                coroutineScope,
                onEvent,
                onClosed,
                onFailure
            )
        )
        return webSocket // Return the WebSocket
    }


    /**
     * Close the WebSocket connection.
     */
    fun closeSocket() {
        Log.d(TAG, "Closing WebSocket connection.")
        isConnected.set(false)
        webSocket?.close(1000, "Client closed connection.")
    }

    /**
     * Send an authorization message to the WebSocket server.
     *
     * @param token Authorization token.
     */
    fun authorize(token: String) {
        if (isConnected.get()) {
            val authMessage = """
                {
                  "type": "authorization",
                  "payload": {
                    "token": "$token"
                  }
                }
            """.trimIndent()
            Log.d(TAG, "Sending authorization message: $authMessage")
            webSocket?.send(authMessage)
        } else {
            Log.w(TAG, "Cannot send authorization message: WebSocket is not connected.")
        }
    }

    /**
     * Send a generic message through WebSocket.
     *
     * @param message The message to send.
     */
    fun sendMessage(event: ProductEvent) {
        if (isConnected.get()) {
            val moshi = Moshi.Builder().build()
            val jsonAdapter = moshi.adapter(ProductEvent::class.java)
            val message = jsonAdapter.toJson(event) // Convert ProductEvent to JSON string
            Log.d(TAG, "Sending message: $message")
            webSocket?.send(message)
        } else {
            Log.w(TAG, "Cannot send message: WebSocket is not connected.")
        }
    }


    /**
     * WebSocket Listener to handle WebSocket events.
     */
    private inner class ProductWebSocketListener(
        private val coroutineScope: CoroutineScope,
        private val onEvent: suspend (productEvent: ProductEvent?) -> Unit,
        private val onClosed: () -> Unit,
        private val onFailure: (Throwable) -> Unit
    ) : WebSocketListener() {

        private val moshi = Moshi.Builder().build()
        private val productEventJsonAdapter: JsonAdapter<ProductEvent> =
            moshi.adapter(ProductEvent::class.java)

        override fun onOpen(webSocket: WebSocket, response: Response) {
            Log.d(TAG, "WebSocket connection established.")
            isConnected.set(true)
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            Log.d(TAG, "Received WebSocket message: $text")
            coroutineScope.launch {
                try {
                    val productEvent = productEventJsonAdapter.fromJson(text)
                    onEvent(productEvent)
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing WebSocket message: $e")
                }
            }
        }

        override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
            Log.d(TAG, "Received WebSocket binary message: $bytes")
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            Log.d(TAG, "WebSocket is closing. Code: $code, Reason: $reason")
            isConnected.set(false)
            webSocket.close(1000, null)
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            Log.d(TAG, "WebSocket connection closed. Code: $code, Reason: $reason")
            isConnected.set(false)
            onClosed()
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            Log.e(TAG, "WebSocket connection failed: ${t.message}")
            isConnected.set(false)
            onFailure(t)
        }
    }
}
