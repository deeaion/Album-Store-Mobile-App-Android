package com.albumstore.todo.ui.products
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.albumstore.core.data.remote.UserPreferencesRepository
import com.albumstore.todo.data.product.ProductRepository
import com.albumstore.todo.ui.product.ProductListItem
import com.albumstore.todo.ui.products.ProductsViewModel
import com.albumstore.utils.WebSocketManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsScreen(
    productRepository: ProductRepository,
    userPreferencesRepository: UserPreferencesRepository,
    webSocketManager: WebSocketManager,
    onProductClick: (String) -> Unit,
    onAddProductClick: () -> Unit
) {
    val productsViewModel: ProductsViewModel = viewModel(
        factory = ProductsViewModel.Factory(productRepository, userPreferencesRepository, webSocketManager)
    )

    val uiState by productsViewModel.uiState.collectAsState()
    val lazyListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    val snackbarHostState = remember { SnackbarHostState() }
    val isAdmin = uiState.userRole.split(",").any { it.trim().equals("Admin", ignoreCase = true) }

    // Pull-to-refresh state
    var refreshing by remember { mutableStateOf(false) }

    // Infinite scroll handling
    LaunchedEffect(lazyListState) {
        snapshotFlow { lazyListState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collectLatest { lastVisibleIndex ->
                if (lastVisibleIndex == uiState.products.size - 1 && !uiState.fetching) {
                    productsViewModel.loadProducts(reset = false)
                }
            }
    }

    // Reset refreshing when fetching completes
    LaunchedEffect(uiState.fetching) {
        if (!uiState.fetching) {
            refreshing = false
        }
    }

    // Handle notifications
    uiState.notification?.let { notification ->
        LaunchedEffect(notification) {
            snackbarHostState.showSnackbar(
                message = notification.message,
                actionLabel = "OK"
            )
            productsViewModel.clearNotification() // Clear notification after showing it
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Products") })
        },
        floatingActionButton = {
            if (isAdmin) {
                FloatingActionButton(onClick = onAddProductClick) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add Product")
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .nestedScroll(object : androidx.compose.ui.input.nestedscroll.NestedScrollConnection {
                    override fun onPreScroll(available: Offset, source: androidx.compose.ui.input.nestedscroll.NestedScrollSource): Offset {
                        if (available.y > 0 && lazyListState.firstVisibleItemIndex == 0 && !refreshing) {
                            refreshing = true
                            productsViewModel.reloadProducts()
                        }
                        return super.onPreScroll(available, source)
                    }
                })
        ) {
            LazyColumn(
                state = lazyListState,
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.products) { product ->
                    ProductListItem(
                        product = product,
                        onClick = { onProductClick(product.id) },
                        onFavoriteClick = { productsViewModel.toggleFavorite(product.id) }
                    )
                }
            }

            if (uiState.fetching && uiState.products.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            if (uiState.products.isEmpty() && !uiState.fetching) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No products available.", style = MaterialTheme.typography.bodyLarge)
                }
            }

            uiState.fetchingError?.let { error ->
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    action = {
                        Button(onClick = { productsViewModel.reloadProducts() }) {
                            Text("Retry")
                        }
                    }
                ) {
                    Text(text = "Error: $error")
                }
            }

            // Show pull-to-refresh indicator
            if (refreshing) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(32.dp))
                }
            }
        }
    }
}
