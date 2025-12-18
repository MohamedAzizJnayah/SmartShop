package com.example.smartshop.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.smartshop.ui.screens.product.ProductsScreen
import com.example.smartshop.ui.viewmodel.authentication.AuthViewModel
import com.example.smartshop.ui.viewmodel.product.ProductViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onLogout: () -> Unit,
    productViewModel: ProductViewModel= hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val user = authViewModel.user.collectAsState().value

    // ✅ navigation safe (pas dans le body direct)
    LaunchedEffect(user) {
        if (user == null) onLogout()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SmartShop") },
                actions = {
                    IconButton(onClick = {
                        authViewModel.logout()
                        onLogout()
                    }) {
                        Icon(Icons.Default.Logout, contentDescription = "Logout")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            ProductsScreen(vm = productViewModel)
        }
    }
}
