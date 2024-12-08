package com.albumstore.todo.data.product

import com.albumstore.utils.database.Converters
import org.json.JSONException
import org.json.JSONObject

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
) {
    fun toJson(): String? {
        return try {
            val json = JSONObject()
            json.put("id", id ?: "")
            json.put("name", name)
            json.put("description", description ?: "")
            json.put("price", price)
            json.put("genre", genre ?: "")
            json.put("numberOfSales", numberOfSales ?: 0)
            json.put("numberOfStock", numberOfStock ?: 0)
            json.put("baseImageUrl", baseImageUrl ?: "")
            json.put("detailsImageUrl", detailsImageUrl ?: "")
            json.put("bandId", bandId ?: "")

            // Convert lists to JSONArray
            json.put("productVersions", productVersions?.let {
                org.json.JSONArray(it.map { version -> version.toJson() })
            } ?: org.json.JSONArray())

            json.put("artistIds", artistIds?.let {
                org.json.JSONArray(it)
            } ?: org.json.JSONArray())

            json.put("artists", artists?.let {
                org.json.JSONArray(it)
            } ?: org.json.JSONArray())

            json.put("createdAt", createdAt ?: "")
            json.put("updatedAt", updatedAt ?: "")
            json.put("bandName", bandName ?: "")
            json.put("isFavorited", isFavorited)

            // Handle baseImage as a JSON object
            json.put("baseImage", Converters.fromImageDto(baseImage) ?: JSONObject())

            json.toString()
        } catch (e: JSONException) {
            e.printStackTrace()
            null
        }
    }

}

