package com.albumstore.todo.data.tasks

import android.content.Context
import android.util.Log
import com.albumstore.core.data.remote.Api
import com.albumstore.todo.data.local.PendingTaskDao
import com.albumstore.todo.data.product.ProductDetail
import com.albumstore.todo.data.remote.FavoriteRequest
import com.albumstore.todo.data.remote.ProductRequest
import com.albumstore.todo.data.remote.ProductService
import kotlinx.coroutines.flow.firstOrNull
import retrofit2.HttpException

class TaskRepository(
    private val context: Context,
    private val taskDao: PendingTaskDao,
    private val productService: ProductService
) {

    suspend fun addPendingTask(task: PendingTask) {
        taskDao.insertTask(task)
        Log.d("TaskRepository", "Task added to pending tasks. ID: ${task.id}")
    }

    fun getPendingTasks() = taskDao.getAllPendingTasks()

    suspend fun deleteTask(taskId: Long) {
        taskDao.deleteTask(taskId)
        Log.d("TaskRepository", "Task deleted. ID: $taskId")
    }

    private fun getBearerToken() = "Bearer ${Api.tokenInterceptor.token}"

    suspend fun processPendingTasks() {
        if (!isOnline()) {
            Log.w("TaskRepository", "Device is offline. Skipping task processing.")
            return
        }

        try {
            val pendingTasks = taskDao.getAllPendingTasks().firstOrNull() ?: emptyList()
            if (pendingTasks.isEmpty()) {
                Log.d("TaskRepository", "No pending tasks to process.")
                return
            }

            for (task in pendingTasks) {
                processTask(task)
            }

            Log.d("TaskRepository", "All pending tasks processed successfully.")
        } catch (e: Exception) {
            Log.e("TaskRepository", "Failed to process pending tasks", e)
        }
    }

    private suspend fun processTask(task: PendingTask) {
        try {
            when (task.taskType) {
                "SAVE_PRODUCT" -> processSaveProductTask(task)
                "TOGGLE_FAVORITE" -> processToggleFavoriteTask(task)
                else -> Log.w("TaskRepository", "Unknown task type: ${task.taskType}")
            }
        } catch (e: Exception) {
            Log.e("TaskRepository", "Error while processing task ID: ${task.id}", e)
        }
    }

    private suspend fun processSaveProductTask(task: PendingTask) {
        task.productData?.let { productJson ->
            val product = parseProductJson(productJson)
            if (product != null) {
                try {
                    productService.createProduct(
                        authorization = getBearerToken(),
                        productRequest = ProductRequest(product)
                    )
                    taskDao.deleteTask(task.id)
                    Log.d("TaskRepository", "Product saved successfully for task ID: ${task.id}")
                } catch (e: HttpException) {
                    Log.e("TaskRepository", "Failed to save product. Retrying later.")
                }
            } else {
                Log.e("TaskRepository", "Invalid product data for task ID: ${task.id}")
            }
        }
    }

    private suspend fun processToggleFavoriteTask(task: PendingTask) {
        val productId = task.productId ?: return

        Log.d("TaskRepository", "Processing toggle favorite for product $productId")

        val favoriteRequest = FavoriteRequest(productId)
        try {
            if (task.isFavorited == true) {
                productService.addProductToFavorites(
                    authorization = getBearerToken(),
                    favoriteRequest = favoriteRequest
                )
                Log.d("TaskRepository", "Added product $productId to favorites")
            } else {
                productService.removeProductFromFavorites(
                    authorization = getBearerToken(),
                    productId = productId
                )
                Log.d("TaskRepository", "Removed product $productId from favorites")
            }
            taskDao.deleteTask(task.id)
        } catch (e: HttpException) {
            Log.e("TaskRepository", "Failed to toggle favorite for product $productId", e)
        }
    }

    private fun parseProductJson(productJson: String): ProductDetail? {
        return try {
            val moshi = com.squareup.moshi.Moshi.Builder().build()
            val adapter = moshi.adapter(ProductDetail::class.java)
            adapter.fromJson(productJson)
        } catch (e: Exception) {
            Log.e("TaskRepository", "Failed to parse product JSON", e)
            null
        }
    }

    private fun isOnline(): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as? android.net.ConnectivityManager
        return connectivityManager?.activeNetworkInfo?.isConnected == true
    }
}
