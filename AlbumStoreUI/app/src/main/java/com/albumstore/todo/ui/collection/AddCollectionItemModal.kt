package com.albumstore.todo.ui.collection

import android.net.Uri
import androidx.camera.core.CameraSelector
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberImagePainter
import com.albumstore.services.camera.CameraCapture
import com.albumstore.services.camera.encodeImageToBase64
import com.albumstore.services.gallery.EMPTY_IMAGE_URI
import com.albumstore.services.gallery.GallerySelect
import com.albumstore.todo.data.band.Band
import com.albumstore.todo.data.band.BandRepository
import com.albumstore.todo.data.collection.CollectionItem
import com.albumstore.todo.data.collection.CollectionItemRepository
import com.albumstore.todo.data.product.ImageDto
import com.albumstore.todo.data.product.Product
import com.albumstore.todo.data.product.ProductRepository
import com.albumstore.todo.data.tasks.TaskRepository
import com.albumstore.todo.ui.band.BandViewModel
import com.albumstore.todo.ui.product.ProductViewModel
import com.albumstore.utils.sockets.WebSocketManager
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import kotlinx.coroutines.launch
import java.io.File
import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.lazy.LazyColumn
import com.albumstore.services.camera.resizeBitmap
import java.io.ByteArrayOutputStream

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun AddCollectionItemScreen(
    collectionRepository: CollectionItemRepository,
    bandRepository: BandRepository,
    productRepository: ProductRepository,
    webSocketManager: WebSocketManager,
    taskRepository: TaskRepository,
    onCollectionSaved: () -> Unit,
    onCancel: () -> Unit
) {
    val collectionViewModel: CollectionViewModel = viewModel(factory = CollectionViewModel.Factory(collectionRepository))
    val bandViewModel: BandViewModel = viewModel(factory = BandViewModel.Factory(bandRepository))
    val productViewModel: ProductViewModel = viewModel(factory = ProductViewModel.Factory(productRepository, webSocketManager, taskRepository))

    val bandUiState by bandViewModel.uiState.collectAsStateWithLifecycle()
    val productUiState by productViewModel.uiState.collectAsStateWithLifecycle()

    var title by remember { mutableStateOf("") }
    var selectedBand by remember { mutableStateOf<Band?>(null) }
    var selectedProduct by remember { mutableStateOf<Product?>(null) }
    var imageUri by remember { mutableStateOf<Uri?>(EMPTY_IMAGE_URI) }
    var encodedImage by remember { mutableStateOf<String?>(null) }

    var bandDropdownExpanded by remember { mutableStateOf(false) }
    var productDropdownExpanded by remember { mutableStateOf(false) }
    var showCamera by remember { mutableStateOf(false) }
    var showGalleryPicker by remember { mutableStateOf(false) }
    var isLoadingImage by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Fetch Bands and Products
    LaunchedEffect(Unit) {
        bandViewModel.fetchBands(online = true)
        productViewModel.loadProducts(reset = false)
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Add Collection Item") }) },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        content = { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
            ) {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("Title") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        // Band Dropdown
                        ExposedDropdownMenuBox(
                            expanded = bandDropdownExpanded,
                            onExpandedChange = { bandDropdownExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = selectedBand?.name ?: "Select a Band",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Band") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                trailingIcon = {
                                    Icon(
                                        imageVector = if (bandDropdownExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.ArrowDropDown,
                                        contentDescription = null
                                    )
                                }
                            )
                            ExposedDropdownMenu(
                                expanded = bandDropdownExpanded,
                                onDismissRequest = { bandDropdownExpanded = false }
                            ) {
                                bandUiState.bands.forEach { band ->
                                    DropdownMenuItem(
                                        text = { Text(band.name) },
                                        onClick = {
                                            selectedBand = band
                                            bandDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    item {
                        // Product Dropdown
                        ExposedDropdownMenuBox(
                            expanded = productDropdownExpanded,
                            onExpandedChange = { productDropdownExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = selectedProduct?.name ?: "Select a Product",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Product") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                trailingIcon = {
                                    Icon(
                                        imageVector = if (productDropdownExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.ArrowDropDown,
                                        contentDescription = null
                                    )
                                }
                            )
                            ExposedDropdownMenu(
                                expanded = productDropdownExpanded,
                                onDismissRequest = { productDropdownExpanded = false }
                            ) {
                                productUiState.products.forEach { product ->
                                    DropdownMenuItem(
                                        text = { Text(product.name) },
                                        onClick = {
                                            selectedProduct = product
                                            productDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    item {
                        // Camera and Gallery Integration
                        val contentResolver = LocalContext.current.contentResolver

                        if (showCamera) {
                            CameraCapture(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(300.dp),
                                cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                            ) { file ->
                                imageUri = Uri.fromFile(file)
                                isLoadingImage = true
                                coroutineScope.launch {
                                    val bitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(imageUri!!))
                                    val resizedBitmap = resizeBitmap(bitmap, 400, 400)
                                    encodedImage = encodeBitmapToBase64(resizedBitmap, Bitmap.CompressFormat.JPEG, 85)
                                    isLoadingImage = false
                                    showCamera = false
                                }
                            }
                        }

                        if (showGalleryPicker) {
                            GallerySelect(
                                modifier = Modifier.fillMaxWidth(),
                                onImageUri = { uri ->
                                    imageUri = uri
                                    uri.let {
                                        isLoadingImage = true
                                        coroutineScope.launch {
                                            val bitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(it))
                                            val resizedBitmap = resizeBitmap(bitmap, 400, 400)
                                            encodedImage = encodeBitmapToBase64(resizedBitmap, Bitmap.CompressFormat.JPEG, 85)
                                            isLoadingImage = false
                                            showGalleryPicker = false
                                        }
                                    }
                                }
                            )

                    }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(onClick = { showCamera = true }) {
                                Text("Open Camera")
                            }

                            Button(onClick = { showGalleryPicker = true }) {
                                Text("Pick from Gallery")
                            }
                        }

                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
                            if (isLoadingImage) {
                                CircularProgressIndicator()
                            } else {
                                imageUri?.let {
                                    Image(
                                        painter = rememberImagePainter(it),
                                        contentDescription = null,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(200.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Save and Cancel Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(onClick = onCancel) { Text("Cancel") }
                    Button(
                        onClick = {
                            if (title.isBlank() || selectedBand == null || selectedProduct == null) {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("All fields are required.")
                                }
                            } else {
                                coroutineScope.launch {
                                    val newCollectionItem = CollectionItem(
                                        id = System.currentTimeMillis().toString(),
                                        title = title,
                                        artist = selectedBand?.name ?: "",
                                        productId = selectedProduct?.id,
                                        image = encodedImage?.let {
                                            ImageDto(
                                                imageBase64 = it,
                                                contentType = "image/jpeg",
                                                fileName = "image.jpg"
                                            )
                                        }
                                    )
                                    collectionViewModel.addCollectionItem(newCollectionItem)
                                    onCollectionSaved()
                                }
                            }
                        }
                    ) { Text("Save") }
                }
            }
        }
    )
}
fun resizeBitmap(originalBitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
    val aspectRatio = originalBitmap.width.toFloat() / originalBitmap.height.toFloat()
    val scaledWidth = if (aspectRatio > 1) maxWidth else (maxHeight * aspectRatio).toInt()
    val scaledHeight = if (aspectRatio > 1) (maxWidth / aspectRatio).toInt() else maxHeight
    return Bitmap.createScaledBitmap(originalBitmap, scaledWidth, scaledHeight, true)
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


