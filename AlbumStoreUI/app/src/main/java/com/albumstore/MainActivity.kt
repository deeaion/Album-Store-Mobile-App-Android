package com.albumstore

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import com.albumstore.core.TAG
import com.albumstore.ui.theme.AlbumStoreUITheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "MainActivity created")

        // Initialize WebSocket connection
        (application as MyApplication).container.initializeWebSocket()

        setContent {
            MyApp {
                MyAppNavHost() // Navigation host for your app
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Close WebSocket when the app is destroyed
        (application as MyApplication).container.closeWebSocket()
    }
}

@Composable
fun MyApp(content: @Composable () -> Unit) {
    Log.d("MyApp", "Recompose triggered")
    AlbumStoreUITheme {
        content()
    }
}
