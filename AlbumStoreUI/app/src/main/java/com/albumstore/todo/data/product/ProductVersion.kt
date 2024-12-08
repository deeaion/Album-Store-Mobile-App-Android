package com.albumstore.todo.data.product

import org.json.JSONObject

data class ProductVersion(
    val id: String,
    val version: String,
    val description: String,
    val imageUrl: String,
    val price: Double,
    val productId: String
) {
    fun toJson(): String {
        // Convert to JSON
        val json = JSONObject()
        json.put("id", id)
        json.put("version", version)
        json.put("description", description)
        json.put("imageUrl", imageUrl)
        json.put("price", price)
        json.put("productId", productId)
        return json.toString()
    }
}
