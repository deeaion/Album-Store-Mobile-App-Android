package com.albumstore.todo.data.remote

import com.albumstore.todo.data.product.Product

data class GetAllProductsFilter(
    val skip: Int? = 0,
    val take: Int? = 1000,
    val sortBy: String? = "id",
    val sortOrder: String? = "asc",
    val search: String? = null,
    val artistName: String? = null,
    val genre: String? = null,
    val artistId: String? = null,
    val bandName: String? = null
)

data class GetAllProductsResponse(
    val records: List<Product>,
    val totalNumberOfRecords: Int
)

fun GetAllProductsFilter.toQueryMap(): Map<String, String> {
    return mapOf(
        "skip" to skip?.toString(),
        "take" to take?.toString(),
        "sortBy" to sortBy,
        "sortOrder" to sortOrder,
        "search" to search,
        "artistName" to artistName,
        "genre" to genre,
        "artistId" to artistId,
        "bandName" to bandName
    ).filterValues { it != null }
        .mapValues { it.value!! }
}