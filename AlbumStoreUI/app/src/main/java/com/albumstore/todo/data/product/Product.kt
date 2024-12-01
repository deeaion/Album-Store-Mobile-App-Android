package com.albumstore.todo.data.product

import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity(tableName = "products")
data class Product(
    @PrimaryKey val id: String,
    val name: String = "Unknown",
    val price: Double = 0.0,
    val bandName: String = "Unknown Band",
    val artistsName: String? = null,
    val image: String? = null,
    val baseImage: ImageDto? = null, // Use ImageDto type here
    val isFavorited: Boolean = false
)



