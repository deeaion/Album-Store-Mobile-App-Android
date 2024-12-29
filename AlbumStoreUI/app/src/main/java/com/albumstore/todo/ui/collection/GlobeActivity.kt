//package com.albumstore.todo.ui.collection
//
//import android.content.Context
//import android.content.Intent
//import android.graphics.Bitmap
//import android.graphics.BitmapFactory
//import android.os.Bundle
//import android.util.Base64
//import android.view.View
//import android.widget.FrameLayout
//import android.widget.ImageView
//import androidx.appcompat.app.AppCompatActivity
//import com.albumstore.R
//import com.albumstore.todo.data.collection.CollectionItem
//import kotlin.math.roundToInt
//import android.hardware.Sensor
//import android.hardware.SensorEvent
//import android.hardware.SensorEventListener
//import android.hardware.SensorManager
//import kotlin.math.min
//import kotlin.math.max
//
//class GlobeActivity : AppCompatActivity(), SensorEventListener {
//
//    companion object {
//        private const val COLLECTION_ITEMS_KEY = "COLLECTION_ITEMS"
//
//        fun start(context: Context, collectionItems: List<CollectionItem>) {
//            val wrappers = collectionItems.map { CollectionItemWrapper.fromCollectionItem(it) }
//            val intent = Intent(context, GlobeActivity::class.java).apply {
//                putParcelableArrayListExtra(COLLECTION_ITEMS_KEY, ArrayList(wrappers))
//            }
//            context.startActivity(intent)
//        }
//    }
//
//    private lateinit var sensorManager: SensorManager
//    private var accelerometer: Sensor? = null
//    private var collectionItemWrappers: List<CollectionItemWrapper>? = null
//    private lateinit var container: FrameLayout
//    private val imageViews = mutableListOf<ImageView>()
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_globe) // A simple layout with a FrameLayout as a container
//
//        container = findViewById(R.id.container)
//        collectionItemWrappers = intent.getParcelableArrayListExtra(COLLECTION_ITEMS_KEY)
//
//        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
//        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
//
//        displayImages()
//    }
//
//    override fun onResume() {
//        super.onResume()
//        accelerometer?.let {
//            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
//        }
//    }
//
//    override fun onPause() {
//        super.onPause()
//        sensorManager.unregisterListener(this)
//    }
//
//    private fun displayImages() {
//        collectionItemWrappers?.forEach { wrapper ->
//            wrapper.imageBase64?.let { base64 ->
//                val bitmap = decodeBase64ToBitmap(base64)
//                bitmap?.let {
//                    val imageView = ImageView(this).apply {
//                        setImageBitmap(it)
//                        layoutParams = FrameLayout.LayoutParams(200, 200).apply {
//                            leftMargin = (Math.random() * container.width).toInt()
//                            topMargin = (Math.random() * container.height).toInt()
//                        }
//                    }
//                    container.addView(imageView)
//                    imageViews.add(imageView)
//                }
//            }
//        }
//    }
//
//    private fun decodeBase64ToBitmap(base64: String): Bitmap? {
//        return try {
//            val decodedBytes = Base64.decode(base64, Base64.DEFAULT)
//            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
//        } catch (e: IllegalArgumentException) {
//            e.printStackTrace()
//            null
//        }
//    }
//
//    override fun onSensorChanged(event: SensorEvent?) {
//        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
//            val x = event.values[0]
//            val y = event.values[1]
//            val z = event.values[2]
//
//            moveImages(x, y)
//        }
//    }
//
//    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
//        // No action needed for this implementation
//    }
//
//    private fun moveImages(x: Float, y: Float) {
//        imageViews.forEach { imageView ->
//            val currentX = imageView.translationX
//            val currentY = imageView.translationY
//
//            // Adjust the movement sensitivity
//            val sensitivity = 5
//
//            // Calculate new positions with boundaries
//            val newX = max(0f, min(currentX - x * sensitivity, container.width.toFloat() - imageView.width))
//            val newY = max(0f, min(currentY + y * sensitivity, container.height.toFloat() - imageView.height))
//
//            imageView.translationX = newX
//            imageView.translationY = newY
//        }
//    }
//}
