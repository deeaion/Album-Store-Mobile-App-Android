package com.albumstore.todo.data.band

import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity(tableName = "bands")
data class Band(
    @PrimaryKey val  id: String,
    val name: String
)
