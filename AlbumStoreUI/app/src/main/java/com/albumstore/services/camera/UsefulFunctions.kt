package com.albumstore.services.camera

import java.io.ByteArrayOutputStream

import android.content.Context
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream
import java.util.concurrent.Executor
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
// Extension function to get the CameraProvider
suspend fun Context.getCameraProvider(): ProcessCameraProvider =
    suspendCoroutine { continuation ->
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener(
            {
                try {
                    continuation.resume(cameraProviderFuture.get())
                } catch (e: Exception) {
                    continuation.resumeWithException(e)
                }
            },
            this.executor
        )
    }

// Property to get the main thread executor
val Context.executor: Executor
    get() = ContextCompat.getMainExecutor(this)

// Function to take a picture using ImageCapture and save it as a temporary file
suspend fun ImageCapture.takePicture(executor: Executor): File {
    return withContext(Dispatchers.IO) {
        val photoFile = kotlin.runCatching {
            File.createTempFile("image_", ".jpg")
        }.getOrElse { ex ->
            println("Error creating temp file: $ex")
            throw IllegalStateException("Could not create temp file")
        }

        suspendCoroutine { continuation ->
            val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
            takePicture(
                outputOptions,
                executor,
                object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                        continuation.resume(photoFile)
                    }

                    override fun onError(ex: ImageCaptureException) {
                        println("Error capturing image: $ex")
                        continuation.resumeWithException(ex)
                    }
                }
            )
        }
    }
}

// Function to encode an image file to Base64


fun encodeImageToBase64(contentResolver: ContentResolver, uri: Uri, maxWidth: Int, maxHeight: Int): String? {
    return try {
        val inputStream = contentResolver.openInputStream(uri)
        val originalBitmap = BitmapFactory.decodeStream(inputStream)
        val scaledBitmap = resizeBitmap(originalBitmap, maxWidth, maxHeight)

        val outputStream = ByteArrayOutputStream()
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        val byteArray = outputStream.toByteArray()
        Base64.encodeToString(byteArray, Base64.DEFAULT)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun resizeBitmap(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
    val width = bitmap.width
    val height = bitmap.height
    val aspectRatio = width.toFloat() / height.toFloat()

    val newWidth = if (width > height) maxWidth else (maxHeight * aspectRatio).toInt()
    val newHeight = if (height > width) maxHeight else (maxWidth / aspectRatio).toInt()

    return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
}


// Overloaded function to encode a File to Base64
fun encodeImageToBase64(file: File): String? {
    return try {
        val bytes = file.readBytes()
        Base64.encodeToString(bytes, Base64.DEFAULT)
    } catch (e: Exception) {
        println("Error encoding file to Base64: ${e.message}")
        e.printStackTrace()
        null
    }
}


fun encodeBitmapToBase64(bitmap: Bitmap, format: Bitmap.CompressFormat, quality: Int): String? {
    return try {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(format, quality, outputStream)
        val byteArray = outputStream.toByteArray()
        android.util.Base64.encodeToString(byteArray, android.util.Base64.DEFAULT)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

suspend fun decodeBase64ToBitmap(base64String: String): Bitmap? = withContext(Dispatchers.IO) {
    try {
        val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}