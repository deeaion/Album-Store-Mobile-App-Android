package com.albumstore.todo.data.local
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.albumstore.todo.data.collection.CollectionItem

@Dao
interface CollectionItemDao {
    @Query("SELECT * FROM collection_items")
    suspend fun getAllCollectionItems(): List<CollectionItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCollectionItem(item: CollectionItem)

    @Query("DELETE FROM collection_items WHERE id = :id")
    suspend fun deleteCollectionItemById(id: String)
}