package com.example.smartshop.ui.screens.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import com.example.smartshop.ui.screens.chat.AIChatScreen
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.smartshop.ui.screens.product.ProductsManagementScreen
import com.example.smartshop.ui.screens.profile.ProfileScreen
import com.example.smartshop.ui.theme.PrimaryGradientStart
import com.example.smartshop.ui.viewmodel.authentication.AuthViewModel
import com.example.smartshop.ui.viewmodel.product.ProductViewModel
data class NavItemData(
    val route: String,
    val label: String,
    val icon: ImageVector
)

@Composable
fun HomeNavigationScreen(
    onLogout: () -> Unit,
    authViewModel: AuthViewModel,
    productViewModel: ProductViewModel
) {
    var currentRoute by remember { mutableStateOf("home") }
    var showAIChat by remember { mutableStateOf(false) }
    val navItems = listOf(
        NavItemData("home", "Home", Icons.Default.Home),
        NavItemData("products", "Produits", Icons.Default.ShoppingCart),
        NavItemData("profile", "Profil", Icons.Default.Person)
    )

    val user by authViewModel.user.collectAsState()
    val userName = user?.email?.split("@")?.get(0) ?: "User"

    Scaffold(
        bottomBar = {
            ModernBottomNavBar(
                currentRoute = currentRoute,
                onNavigate = { route -> currentRoute = route },
                items = navItems
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAIChat = true },
                containerColor = PrimaryGradientStart,
                modifier = Modifier.padding(bottom = 80.dp)
            ) {
                Icon(
                    Icons.Default.SmartToy,
                    contentDescription = "AI Chat",
                    tint = Color.White
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (currentRoute) {
                "home" -> {
                    val state by productViewModel.state.collectAsState()
                    StatisticsScreen(
                        products = state.products,
                        userName = userName,
                        isLoading = state.isLoading
                    )
                }
                "products" -> {
                    ProductsManagementScreen(viewModel = productViewModel)
                }
                "profile" -> {
                    ProfileScreen(onLogout = onLogout, authViewModel = authViewModel)
                }
            }
        }
    }
    // Show AI Chat Screen
    if (showAIChat) {
        AIChatScreen(onClose = { showAIChat = false })
    }
}

@Composable
fun ModernBottomNavBar(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier,
    items: List<NavItemData>
) {
    NavigationBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                selected = currentRoute == item.route,
                onClick = { onNavigate(item.route) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = PrimaryGradientStart,
                    selectedTextColor = PrimaryGradientStart,
                    indicatorColor = PrimaryGradientStart.copy(alpha = 0.1f)
                )
            )
        }
    }
}