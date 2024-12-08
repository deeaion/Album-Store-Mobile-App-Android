package com.albumstore.utils.database

import androidx.room.TypeConverter
import com.albumstore.todo.data.product.ImageDto
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {

    private val gson = Gson()

    @TypeConverter
    fun fromImageDto(imageDto: ImageDto?): String? {
        return imageDto?.let { gson.toJson(it) }
    }

    @TypeConverter
    fun toImageDto(imageDtoString: String?): ImageDto? {
        return imageDtoString?.let {
            gson.fromJson(it, object : TypeToken<ImageDto>() {}.type)
        }
    }

    companion object {
        fun fromImageDto(baseImage: ImageDto?): String? {
            return baseImage?.let { Gson().toJson(it) }
        }
        fun toImageDto(baseImageString: String?): ImageDto? {
            return baseImageString?.let {
                Gson().fromJson(it, ImageDto::class.java)
            }
        }
    }



}
