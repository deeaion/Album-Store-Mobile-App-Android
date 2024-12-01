package com.albumstore.todo.data.product

data class ProductVersion(
    val id: String,
    val version: String,
    val description: String,
    val imageUrl: String,
    val price: Double,
    val productId: String
)
