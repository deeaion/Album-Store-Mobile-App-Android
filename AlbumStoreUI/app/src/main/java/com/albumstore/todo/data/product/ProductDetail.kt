package com.albumstore.todo.data.product

data class ProductDetail(
    val id: String?,
    val name: String,
    val description: String? = null,
    val price: Double,
    val genre: String? = null,
    val numberOfSales: Int? = null,
    val numberOfStock: Int? = null,
    val baseImageUrl: String? = null,
    val detailsImageUrl: String? = null,
    val bandId: String? = null,
    val productVersions: List<ProductVersion>? = null,
    val artistIds: List<String>? = null,
    val artists: List<String>? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val bandName: String? = null,
    val isFavorited: Boolean = false,
    val baseImage: ImageDto? = null,
)

