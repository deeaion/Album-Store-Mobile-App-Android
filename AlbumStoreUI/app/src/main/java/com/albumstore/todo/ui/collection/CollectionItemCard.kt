package com.albumstore.todo.ui.collection

import android.util.Base64
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.albumstore.R
import com.albumstore.services.camera.decodeBase64ToBitmap
import com.albumstore.todo.data.collection.CollectionItem

@Composable
fun CollectionItemCard(item: CollectionItem, onDelete: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Title and Artist
            Text(text = item.title, style = MaterialTheme.typography.titleLarge)
            Text(text = item.artist, style = MaterialTheme.typography.bodyMedium)

            // Animated Image Loading
            val decodedBitmap = produceState<Bitmap?>(initialValue = null, key1 = item.image?.imageBase64) {
                value = item.image?.imageBase64?.let { decodeBase64ToBitmap(it) }
            }

            if (decodedBitmap.value != null) {
                // Animate Image Visibility (Fade-in Effect)
                AnimatedVisibility(visible = true) {
                    Image(
                        bitmap = decodedBitmap.value!!.asImageBitmap(),
                        contentDescription = item.title,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(4f / 3f)
                            .heightIn(max = 150.dp)
                    )
                }
            } else {
                // Shimmer Effect while Loading
                LoadingPlaceholder()
            }

            // Delete Icon
            IconButton(onClick = onDelete, modifier = Modifier.align(Alignment.End)) {
                Icon(Icons.Default.Delete, contentDescription = "Delete Item")
            }
        }
    }
}

@Composable
fun LoadingPlaceholder() {
    val infiniteTransition = rememberInfiniteTransition()
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(4f / 3f)
            .heightIn(max = 150.dp)
            .background(Color.LightGray.copy(alpha = alpha)),
        contentAlignment = Alignment.Center
    ) {
        // Optional text or icon in the middle
        Text("Loading...", color = Color.DarkGray, style = MaterialTheme.typography.bodySmall)
    }
}
