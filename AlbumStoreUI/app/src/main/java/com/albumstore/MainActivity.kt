package com.albumstore

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.albumstore.core.TAG
import com.albumstore.ui.theme.AlbumStoreUITheme
import com.albumstore.utils.notifications.createNotificationChannel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "MainActivity created")

        // Initialize WebSocket connection
        (application as MyApplication).container.initializeWebSocket()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = android.Manifest.permission.POST_NOTIFICATIONS
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(permission), 101)
            }
        }

        createNotificationChannel("product_channel", this)
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
