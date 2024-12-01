package com.albumstore.todo.ui.products.addproduct


import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.albumstore.todo.data.band.Band
import com.albumstore.todo.data.product.ProductDetail
import com.albumstore.todo.data.product.ProductRepository
import com.albumstore.todo.data.remote.ProductWsClient
import com.albumstore.todo.ui.product.ProductViewModel
import kotlinx.coroutines.launch
import com.albumstore.todo.data.band.BandRepository
import com.albumstore.todo.data.product.Product
import com.albumstore.todo.ui.band.BandViewModel
import com.albumstore.utils.WebSocketManager
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductScreen(
    productRepository: ProductRepository,
    webSocketManager: WebSocketManager,
    bandRepository: BandRepository,
    productId: String? = null, // Accept an optional productId
    onProductSaved: () -> Unit,
    onCancel: () -> Unit
) {
    val productViewModel: ProductViewModel = viewModel(factory = ProductViewModel.Factory(productRepository, webSocketManager))
    val bandViewModel: BandViewModel = viewModel(factory = BandViewModel.Factory(bandRepository))
    val _updateEventFlow = MutableSharedFlow<ProductDetail>() // SharedFlow for update events
    val updateEventFlow = _updateEventFlow.asSharedFlow() // Expose as read-only
    val productUiState by productViewModel.uiState.collectAsState()
    val bandUiState by bandViewModel.uiState.collectAsState()

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() } // Snackbar host state for error messages

    // Form state
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var genre by remember { mutableStateOf("") }
    var numberOfStock by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var selectedBand by remember { mutableStateOf<Band?>(null) }

    // Dropdown menu state
    var dropdownExpanded by remember { mutableStateOf(false) }

    // Load bands asynchronously
    LaunchedEffect(Unit) {
        bandViewModel.fetchBands(online = true)
    }

    // Load product details if `productId` is provided
    LaunchedEffect(productId) {
        productId?.let {
            productViewModel.loadProductDetail(it)
        }
    }

    // Update form state with product details when loaded
    LaunchedEffect(productUiState.productDetail) {
        productUiState.productDetail?.let { productDetail ->
            name = productDetail.name ?: ""
            description = productDetail.description ?: ""
            genre = productDetail.genre ?: ""
            numberOfStock = productDetail.numberOfStock.toString()
            price = productDetail.price.toString()
            selectedBand = bandUiState.bands.find { it.id == productDetail.bandId }
        }
    }

    // Show error message in Snackbar if saving error occurs
    LaunchedEffect(productUiState.savingError) {
        productUiState.savingError?.let { error ->
            snackbarHostState.showSnackbar(message = error)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(if (productId == null) "Add Product" else "Update Product") })
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) // Attach SnackbarHost for error messages
        },
        content = { padding ->
            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Name Input
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Name") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Description Input
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Genre Input
                    OutlinedTextField(
                        value = genre,
                        onValueChange = { genre = it },
                        label = { Text("Genre") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Number of Stock Input
                    OutlinedTextField(
                        value = numberOfStock,
                        onValueChange = { numberOfStock = it },
                        label = { Text("Number of Stock") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Price Input
                    OutlinedTextField(
                        value = price,
                        onValueChange = { price = it },
                        label = { Text("Price") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Band Dropdown
                    if (bandUiState.fetching) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                    } else {
                        ExposedDropdownMenuBox(
                            expanded = dropdownExpanded,
                            onExpandedChange = { dropdownExpanded = it }
                        ) {
                            OutlinedTextField(
                                readOnly = true,
                                value = selectedBand?.name ?: "Select a Band",
                                onValueChange = {},
                                label = { Text("Band") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                trailingIcon = {
                                    Icon(
                                        imageVector = if (dropdownExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.ArrowDropDown,
                                        contentDescription = null
                                    )
                                }
                            )

                            ExposedDropdownMenu(
                                expanded = dropdownExpanded,
                                onDismissRequest = { dropdownExpanded = false }
                            ) {
                                bandUiState.bands.forEach { band ->
                                    DropdownMenuItem(
                                        text = { Text(band.name) },
                                        onClick = {
                                            selectedBand = band
                                            dropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Button(onClick = onCancel) {
                            Text("Cancel")
                        }

                        Button(
                            onClick = {
                                val productDetail = ProductDetail(
                                    id = productId, // Pass `productId` to update if available
                                    name = name,
                                    description = description,
                                    genre = genre,
                                    numberOfStock = numberOfStock.toIntOrNull() ?: 0,
                                    price = price.toDoubleOrNull() ?: 0.0,
                                    bandId = selectedBand?.id ?: ""
                                )

                                if (name.isBlank() || description.isBlank() || genre.isBlank() || selectedBand == null) {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("All fields are required.")
                                    }
                                } else {
                                    scope.launch {
                                        productViewModel.saveProduct(productDetail)
                                        onProductSaved()
                                        _updateEventFlow.emit(productDetail) // Emit update event
                                    }

                                }
                            },
                            enabled = !productUiState.saving
                        ) {
                            if (productUiState.saving) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Text(if (productId == null) "Save" else "Update")
                            }
                        }
                    }
                }
            }
        }
    )
}
