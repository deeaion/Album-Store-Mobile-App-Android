package com.albumstore.todo.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.albumstore.todo.data.tasks.PendingTask
import kotlinx.coroutines.flow.Flow

@Dao
interface PendingTaskDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: PendingTask)

    @Query("SELECT * FROM pending_tasks")
    fun getAllPendingTasks(): Flow<List<PendingTask>>

    @Query("DELETE FROM pending_tasks WHERE id = :taskId")
    suspend fun deleteTask(taskId: Long)

    @Query("DELETE FROM pending_tasks")
    suspend fun deleteAllTasks()
}