package com.albumstore.todo.ui.product

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.albumstore.R
import com.albumstore.core.data.remote.UserPreferencesRepository
import com.albumstore.todo.data.product.ProductDetail
import com.albumstore.todo.data.product.ProductRepository
import com.albumstore.todo.data.remote.ProductWsClient
import com.albumstore.utils.WebSocketManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductScreen(
    productId: String?,
    productRepository: ProductRepository,
    webSocketManager: WebSocketManager,
    userPreferencesRepository: UserPreferencesRepository,
    onBackClick: () -> Unit,
    onEditClick: (String) -> Unit // Add a callback for editing
) {
    if (productId == null) {
        // Handle missing productId case (e.g., for new product)
        return
    }

    // Create ViewModel instance
    val productViewModel: ProductViewModel = viewModel(
        factory = ProductViewModel.Factory(productRepository, webSocketManager)
    )

    // Collect UI state
    val uiState by productViewModel.uiState.collectAsState()
    val productDetail = uiState.productDetail
    var quantity by remember { mutableIntStateOf(1) } // Replace mutableStateOf with mutableIntStateOf

    // State to track if the user is an admin
    var isAdmin by remember { mutableStateOf(false) }

    // Load product details when the screen is opened
    LaunchedEffect(productId) {
        productViewModel.loadProductDetail(productId)
    }

    // Check if the user is an admin
    LaunchedEffect(Unit) {
        userPreferencesRepository.userPreferencesStream.collect { userPreferences ->
            isAdmin = userPreferences.roles.split(",").any { it.trim().equals("Admin", ignoreCase = true) }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(productDetail?.name ?: "Loading...") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (isAdmin) {
                        IconButton(onClick = { onEditClick(productId) }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Product"
                            )
                        }
                    }
                }

            )
        }
    ) { paddingValues ->
        when {
            uiState.fetching -> {
                // Show loading indicator
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            productDetail == null -> {
                // Show error message if product details are not found
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Product not found.")
                }
            }
            else -> {
                // Display product details
                ProductDetailContent(
                    productDetail = productDetail,
                    quantity = quantity,
                    onQuantityChange = { newQuantity -> quantity = newQuantity }
                )
            }
        }
    }
}
