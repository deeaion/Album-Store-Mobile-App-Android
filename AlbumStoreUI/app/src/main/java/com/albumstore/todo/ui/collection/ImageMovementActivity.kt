package com.albumstore.todo.ui.collection

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Base64
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.albumstore.R
import com.albumstore.todo.data.collection.CollectionItem

class ImageMovementActivity : AppCompatActivity() {

    companion object {
        private const val COLLECTION_ITEMS_KEY = "COLLECTION_ITEMS"

        fun start(context: Context, collectionItems: List<CollectionItem>) {
            val wrappers = collectionItems.map { CollectionItemWrapper.fromCollectionItem(it) }
            val intent = Intent(context, ImageMovementActivity::class.java).apply {
                putParcelableArrayListExtra(COLLECTION_ITEMS_KEY, ArrayList(wrappers))
            }
            context.startActivity(intent)
        }
    }

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var collectionItemWrappers: List<CollectionItemWrapper>? = null
    private lateinit var imageContainer: FrameLayout
    private val imageViews = mutableListOf<ImageView>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_movement)

        // Set up back button in the toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        imageContainer = findViewById(R.id.image_container)

        // Wait for the layout to be fully laid out
        imageContainer.post {
            collectionItemWrappers = intent.getParcelableArrayListExtra(COLLECTION_ITEMS_KEY)

            // Add images dynamically to the container
            collectionItemWrappers?.forEach { wrapper ->
                val bitmap = decodeBase64ToBitmap(wrapper.imageBase64 ?: return@forEach)
                bitmap?.let { addImageToContainer(it) }
            }
        }

        // Set up sensor manager
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    }

    override fun onResume() {
        super.onResume()
        accelerometer?.let {
            sensorManager.registerListener(sensorEventListener, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(sensorEventListener)
    }

    private val sensorEventListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            val x = event.values[0]
            val y = event.values[1]

            imageViews.forEach { imageView ->
                val randomOffsetX = (Math.random() - 0.5).toFloat() * 5f // Smaller random variance for X
                val randomOffsetY = (Math.random() - 0.5).toFloat() * 5f // Smaller random variance for Y

                val newX = (imageView.translationX - x * 1.5f + randomOffsetX)
                    .coerceIn(0f, imageContainer.width.toFloat() - imageView.width)
                val newY = (imageView.translationY + y * 1.5f + randomOffsetY)
                    .coerceIn(0f, imageContainer.height.toFloat() - imageView.height)

                imageView.translationX = newX
                imageView.translationY = newY
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    private fun addImageToContainer(bitmap: Bitmap) {
        val imageView = ImageView(this).apply {
            setImageBitmap(bitmap)
            layoutParams = FrameLayout.LayoutParams(100, 100).apply {
                marginStart = 16
                topMargin = 16
            }
            scaleType = ImageView.ScaleType.CENTER_CROP
        }

        imageViews.add(imageView)
        imageContainer.addView(imageView)

        // Random initial position with valid ranges
        val offsetX = (Math.random() * (imageContainer.width - 150)).toFloat() + 50
        val offsetY = (Math.random() * (imageContainer.height - 150)).toFloat() + 50
        imageView.translationX = offsetX
        imageView.translationY = offsetY
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
}
