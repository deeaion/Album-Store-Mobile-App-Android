package com.albumstore.todo.data.remote;

import com.albumstore.todo.data.product.Product;

import com.squareup.moshi.Json

data class ProductEvent(
    @Json(name = "Type") val type: String,
    @Json(name = "Message") val message: String,
    @Json(name = "ProductName") val productName: String
)
