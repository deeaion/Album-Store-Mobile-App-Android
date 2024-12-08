package com.albumstore.utils.workermanager

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.albumstore.MyAppDatabase
import com.albumstore.core.data.remote.Api
import com.albumstore.todo.data.remote.ProductService
import com.albumstore.todo.data.tasks.TaskRepository
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock


import com.albumstore.utils.notifications.createNotificationChannel
import com.albumstore.utils.notifications.showSimpleNotification


class ProductSyncWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    private val mutex = Mutex()
    private val notificationChannelId = "product_reminder_channel"
    private val notificationId = 1001

    override suspend fun doWork(): Result {
        createNotificationChannel(notificationChannelId, applicationContext)
        sendProductReminderNotification()

        return try {
            val taskRepository = TaskRepository(
                applicationContext,
                MyAppDatabase.getDatabase(applicationContext).taskDao(),
                Api.retrofit.create(ProductService::class.java)
            )

            Log.d("ProductSyncWorker", "Syncing pending tasks...")

            mutex.withLock {
                taskRepository.processPendingTasks()
            }

            Log.d("ProductSyncWorker", "Successfully synced pending tasks.")
            Result.success()
        } catch (e: Exception) {
            Log.e("ProductSyncWorker", "Work failed. Retrying...", e)
            Result.retry()
        }
    }

    private fun sendProductReminderNotification() {
        showSimpleNotification(
            context = applicationContext,
            channelId = notificationChannelId,
            notificationId = notificationId,
            title = "Hey! 👋",
            content = "Would you like to check out our products again?"
        )
    }
}
