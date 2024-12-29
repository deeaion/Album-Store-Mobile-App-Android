package com.albumstore.todo.ui.collection

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlobeScreen(
    viewModel: CollectionViewModel = viewModel(),
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Interactive Images") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (uiState.fetching) {
                CircularProgressIndicator()
            } else if (uiState.items.isEmpty()) {
                Text("No items to display. Add some to interact!")
            } else {
                // Add logic to display the images with interactive motion
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    // Trigger interaction logic (replace this with your image movement UI/logic)
                    Text("Images are ready for interaction! Shake your device.")
                }
            }
        }
    }
}
