package com.albumstore.utils.conectivitymanager

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import androidx.core.content.getSystemService
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.albumstore.utils.workermanager.ProductSyncWorker
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate

@OptIn(ExperimentalCoroutinesApi::class)
class ConnectivityManagerNetworkMonitor(val context: Context) {

    val isOnline: Flow<Boolean> = callbackFlow {
        val connectivityManager = context.getSystemService<ConnectivityManager>()

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                channel.trySend(true)
                enqueueSyncWork()
            }

            override fun onLost(network: Network) {
                channel.trySend(false)
            }
        }

        connectivityManager?.registerNetworkCallback(
            NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build(),
            callback
        )

        channel.trySend(connectivityManager.isCurrentlyConnected())

        awaitClose {
            connectivityManager?.unregisterNetworkCallback(callback)
        }
    }.conflate()

    @SuppressLint("ObsoleteSdkInt")
    @Suppress("DEPRECATION")
    private fun ConnectivityManager?.isCurrentlyConnected(): Boolean {
        return this?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                activeNetwork
                    ?.let(::getNetworkCapabilities)
                    ?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    ?: false
            } else {
                activeNetworkInfo?.isConnected ?: false
            }
        } ?: false
    }

    private fun enqueueSyncWork() {
        val workManager = WorkManager.getInstance(context)
        val workRequest = OneTimeWorkRequest.Builder(ProductSyncWorker::class.java).build()
        workManager.enqueueUniqueWork(
            "ProductSyncWorker",
            ExistingWorkPolicy.KEEP, // Prevent duplicate jobs
            workRequest
        )
    }
}
