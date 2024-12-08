package com.albumstore

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.albumstore.auth.LoginScreen
import com.albumstore.core.data.UserPreferences
import com.albumstore.core.ui.UserPreferencesViewModel
import com.albumstore.todo.ui.product.ProductScreen
import com.albumstore.todo.ui.products.ProductsScreen
import com.albumstore.todo.ui.products.addproduct.AddProductScreen

const val PRODUCTS_ROUTE = "products"
const val ADD_PRODUCT_ROUTE = "product-new"
const val AUTH_ROUTE = "auth"

@Composable
fun MyAppNavHost() {
    val navController = rememberNavController()

    val appContainer = (LocalContext.current.applicationContext as MyApplication).container
    val productRepository = appContainer.productRepository
    val userPreferencesRepository = appContainer.userPreferencesRepository

    val userPreferencesViewModel = viewModel<UserPreferencesViewModel>(factory = UserPreferencesViewModel.Factory)
    val userPreferencesUiState by userPreferencesViewModel.uiState.collectAsState(initial = UserPreferences())
    val taskRepository = appContainer.taskRepository
    NavHost(navController = navController, startDestination = AUTH_ROUTE) {
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
                bandRepository = appContainer.bandRepository,
                taskRepository = taskRepository,
                productId = productId, // Null for adding, non-null for editing
                onProductSaved = { navController.popBackStack(PRODUCTS_ROUTE, false) },
                onCancel = { navController.popBackStack(PRODUCTS_ROUTE, false) }
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
    }

    // Automatically navigate to products screen if user is logged in
    LaunchedEffect(userPreferencesUiState) {
        if (userPreferencesUiState.token.isNotEmpty()) {
            navController.navigate(PRODUCTS_ROUTE) { popUpTo(0) }
        }
    }
}
