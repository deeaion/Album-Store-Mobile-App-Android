package com.albumstore.todo.data.local

import com.albumstore.todo.data.band.Band
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface BandDao {
    @Query("SELECT * FROM Bands")
    suspend fun getAllBands(): List<Band>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBands(bands: List<Band>)
    @Query("DELETE FROM Bands")
    suspend fun deleteAll()
}
