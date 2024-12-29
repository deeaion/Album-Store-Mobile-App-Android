package com.albumstore.todo.ui.collection

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Parcel
import android.os.Parcelable
import android.util.Base64
import com.albumstore.todo.data.collection.CollectionItem
import kotlinx.parcelize.Parcelize
import java.io.ByteArrayOutputStream
@Parcelize
data class CollectionItemWrapper(
    val id: String,
    val title: String,
    val imageBase64: String? // Only the fields needed for transfer
) : Parcelable {
    companion object {
        fun fromCollectionItem(item: CollectionItem): CollectionItemWrapper {
            val optimizedBase64 = item.image?.imageBase64?.let {
                val bitmap = decodeBase64ToBitmap(it)
                bitmap?.resizeAndCompress(100, 100)?.toBase64String()
            }
            return CollectionItemWrapper(
                id = item.id,
                title = item.title,
                imageBase64 = optimizedBase64
            )
        }

        private fun decodeBase64ToBitmap(base64: String): Bitmap? {
            return try {
                val decodedBytes = Base64.decode(base64, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
                null
            }
        }

        private fun Bitmap.resizeAndCompress(targetWidth: Int, targetHeight: Int, quality: Int = 80): Bitmap {
            val matrix = Matrix().apply {
                postScale(targetWidth.toFloat() / width, targetHeight.toFloat() / height)
            }
            return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true).apply {
                compress(Bitmap.CompressFormat.JPEG, quality, ByteArrayOutputStream())
            }
        }

        private fun Bitmap.toBase64String(): String {
            val outputStream = ByteArrayOutputStream()
            compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
            return Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT)
        }
    }


}
