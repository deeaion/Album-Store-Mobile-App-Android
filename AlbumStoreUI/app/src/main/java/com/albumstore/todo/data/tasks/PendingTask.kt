package com.albumstore.todo.data.tasks

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pending_tasks")
data class PendingTask(
    @PrimaryKey(autoGenerate = true) val id: Long = 0, // Unique task ID
    val taskType: String, // Type of task ("SAVE_PRODUCT", "TOGGLE_FAVORITE")
    val productData: String? = null, // Serialized ProductDetail for saving or updating
    val productId: String? = null, // For favorite toggling
    val isFavorited: Boolean? = null, // For toggling favorite status
)
