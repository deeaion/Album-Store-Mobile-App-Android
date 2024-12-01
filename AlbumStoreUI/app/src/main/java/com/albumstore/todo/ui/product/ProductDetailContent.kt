package com.albumstore.todo.ui.product

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.albumstore.R
import com.albumstore.todo.data.product.ProductDetail

@Composable
fun ProductDetailContent(
    productDetail: ProductDetail,
    quantity: Int,
    onQuantityChange: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Product Image
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_background),
            contentDescription = "Product Image",
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(Color.LightGray)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Product Details
        Text(text = productDetail.name, style = MaterialTheme.typography.headlineMedium)
        Text(text = "Price: $${productDetail.price}", style = MaterialTheme.typography.bodyLarge)
        Text(text = productDetail.description ?: "No description", style = MaterialTheme.typography.bodyMedium)

        Spacer(modifier = Modifier.height(16.dp))

        // Quantity Selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Quantity", style = MaterialTheme.typography.bodyLarge)
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { if (quantity > 1) onQuantityChange(quantity - 1) }) {
                    Text("-")
                }
                OutlinedTextField(
                    value = quantity.toString(),
                    onValueChange = {
                        val newQuantity = it.toIntOrNull()?.coerceIn(1, 50) ?: 1
                        onQuantityChange(newQuantity)
                    },
                    modifier = Modifier.width(60.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                IconButton(onClick = { if (quantity < 50) onQuantityChange(quantity + 1) }) {
                    Text("+")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Add to Basket Button
        Button(
            onClick = {
                // Handle add to basket logic here
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text("Add to Basket")
        }
    }
}
