package com.albumstore

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.albumstore.auth.LoginScreen
import com.albumstore.core.data.UserPreferences
import com.albumstore.core.ui.UserPreferencesViewModel
import com.albumstore.todo.ui.collection.CollectionScreen
import com.albumstore.todo.ui.collection.CollectionViewModel
import com.albumstore.todo.ui.products.ProductsScreen
import com.albumstore.todo.ui.products.addproduct.AddProductScreen
import com.albumstore.todo.ui.product.ProductScreen
import com.albumstore.todo.ui.collection.AddCollectionItemScreen
import kotlinx.coroutines.launch

const val PRODUCTS_ROUTE = "products"
const val ADD_PRODUCT_ROUTE = "product-new"
const val COLLECTION_ROUTE = "collection"
const val ADD_COLLECTION_ITEM_ROUTE = "collection-new"
const val AUTH_ROUTE = "auth"
const val SETTINGS_ROUTE = "settings"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyAppNavHost() {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    val appContainer = (LocalContext.current.applicationContext as MyApplication).container
    val productRepository = appContainer.productRepository
    val collectionRepository = appContainer.collectionRepository
    val bandRepository = appContainer.bandRepository
    val userPreferencesRepository = appContainer.userPreferencesRepository

    val userPreferencesViewModel = viewModel<UserPreferencesViewModel>(factory = UserPreferencesViewModel.Factory)
    val userPreferencesUiState by userPreferencesViewModel.uiState.collectAsState(initial = UserPreferences())
    val taskRepository = appContainer.taskRepository

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "AlbumStore Navigation",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                HorizontalDivider()

                NavigationDrawerItem(
                    icon = { Icon(Icons.Filled.Home, contentDescription = "Products") },
                    label = { Text("Products") },
                    selected = false,
                    onClick = {
                        coroutineScope.launch { drawerState.close() }
                        navController.navigate(PRODUCTS_ROUTE) { popUpTo(0) }
                    }
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Filled.Settings, contentDescription = "Collection") },
                    label = { Text("My Collection") },
                    selected = false,
                    onClick = {
                        coroutineScope.launch { drawerState.close() }
                        navController.navigate(COLLECTION_ROUTE) { popUpTo(0) }
                    }
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Filled.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") },
                    selected = false,
                    onClick = {
                        coroutineScope.launch { drawerState.close() }
                        navController.navigate(SETTINGS_ROUTE)
                    }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("AlbumStore") },
                    navigationIcon = {
                        IconButton(onClick = { coroutineScope.launch { drawerState.open() } }) {
                            Icon(Icons.Filled.Menu, contentDescription = "Menu")
                        }
                    }
                )
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = AUTH_ROUTE,
                modifier = Modifier.padding(innerPadding)
            ) {
                // Products list screen
                composable(PRODUCTS_ROUTE) {
                    ProductsScreen(
                        onProductClick = { productId -> navController.navigate("$PRODUCTS_ROUTE/$productId") },
                        onAddProductClick = { navController.navigate("$ADD_PRODUCT_ROUTE/new") },
                        productRepository = productRepository,
                        userPreferencesRepository = userPreferencesRepository,
                        webSocketManager = appContainer.webSocketManager,
                        taskRepository = taskRepository,
                        connectivityManager = appContainer.connectivityManager
                    )
                }

                // Product detail screen with option for editing
                composable("$PRODUCTS_ROUTE/{id}", arguments = listOf(
                    navArgument("id") { type = NavType.StringType; nullable = false }
                )) { backStackEntry ->
                    val productId = backStackEntry.arguments?.getString("id") ?: ""
                    ProductScreen(
                        productId = productId,
                        productRepository = productRepository,
                        webSocketManager = appContainer.webSocketManager,
                        userPreferencesRepository = userPreferencesRepository,
                        onEditClick = { navController.navigate("$ADD_PRODUCT_ROUTE/$productId") },
                        taskRepository = taskRepository,
                        onBackClick = { navController.popBackStack(PRODUCTS_ROUTE, false) }
                    )
                }

                // Add or edit product screen
                composable("$ADD_PRODUCT_ROUTE/{productId}", arguments = listOf(
                    navArgument("productId") { type = NavType.StringType; nullable = true }
                )) { backStackEntry ->
                    val productId = backStackEntry.arguments?.getString("productId")
                    AddProductScreen(
                        productRepository = productRepository,
                        webSocketManager = appContainer.webSocketManager,
                        bandRepository = bandRepository,
                        taskRepository = taskRepository,
                        productId = productId, // Null for adding, non-null for editing
                        onProductSaved = { navController.popBackStack(PRODUCTS_ROUTE, false) },
                        onCancel = { navController.popBackStack(PRODUCTS_ROUTE, false) }
                    )
                }

                // Collection screen
                composable(COLLECTION_ROUTE) {
                    val collectionViewModel: CollectionViewModel = viewModel(factory = CollectionViewModel.Factory(collectionRepository))
                    CollectionScreen(
                        viewModel = collectionViewModel,
                        onAddItemClick = { navController.navigate(ADD_COLLECTION_ITEM_ROUTE) },
                        onItemDelete = { itemId ->
                            collectionViewModel.deleteCollectionItemById(itemId)
                        }
                    )
                }

                // Add Collection Item Screen
                composable(ADD_COLLECTION_ITEM_ROUTE) {
                    AddCollectionItemScreen(
                        collectionRepository = collectionRepository,
                        bandRepository = bandRepository,
                        productRepository = productRepository,
                        onCollectionSaved = { navController.popBackStack(COLLECTION_ROUTE, false) },
                        onCancel = { navController.popBackStack(COLLECTION_ROUTE, false) },
                        webSocketManager = appContainer.webSocketManager,
                        taskRepository = taskRepository
                    )
                }

                // Login screen
                composable(AUTH_ROUTE) {
                    LoginScreen(
                        onClose = {
                            navController.navigate(PRODUCTS_ROUTE) { popUpTo(0) }
                        }
                    )
                }

                // Settings screen placeholder
                composable(SETTINGS_ROUTE) {
                    Text("Settings Screen - Work in Progress", modifier = Modifier.padding(16.dp))
                }
            }
        }
    }

    // Automatically navigate to products screen if user is logged in
    LaunchedEffect(userPreferencesUiState) {
        if (userPreferencesUiState.token.isNotEmpty()) {
            navController.navigate(PRODUCTS_ROUTE) { popUpTo(0) }
        }
    }
}
