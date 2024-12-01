package com.albumstore.todo.ui.product

import android.util.Base64
import android.util.Log
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.albumstore.todo.data.product.Product

typealias OnProductClick = (id: String) -> Unit

@Composable
fun ProductList(
    productList: List<Product>,
    onProductClick: OnProductClick,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(productList) { product ->
            ProductListItem(product, onProductClick, onFavoriteClick = {})
        }
    }
}

@Composable
fun ProductListItem(
    product: Product,
    onClick: (String) -> Unit,
    onFavoriteClick: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(product.id) },
        colors = CardDefaults.cardColors(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Product Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.name ?: "Unknown",
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Price: $${product.price}",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            IconButton(
                onClick = { onFavoriteClick(product.id) },
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Icon(
                    imageVector = if (product.isFavorited) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = if (product.isFavorited) "Unfavorite" else "Favorite"
                )
            }
        }
    }
}

@Composable
fun ProductImage(product: Product) {

    val bitmap = product.baseImage?.imageBase64?.let { base64Image ->
        try {
            val imageBytes = Base64.decode(base64Image, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        } catch (e: Exception) {
            Log.e("ProductListItem", "Failed to decode image", e)
            null
        }
    }

    if (bitmap != null) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "${product.name} image",
            modifier = Modifier
                .size(64.dp)
                .padding(end = 12.dp)
                .background(Color.LightGray) // Placeholder background
        )
    } else {
        Box(
            modifier = Modifier
                .size(64.dp)
                .padding(end = 12.dp)
                .background(Color.LightGray), // Placeholder for missing image
            contentAlignment = Alignment.Center
        ) {
            Text(text = "No Image", fontSize = 12.sp, color = Color.Gray)
        }
    }
}

@Composable
fun ProductDetails(product: Product) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = product.name ?: "Unknown",
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Price: $${product.price}",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.secondary
        )
        Text(
            text = "Band: ${product.bandName}",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
