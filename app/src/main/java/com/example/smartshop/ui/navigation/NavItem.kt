package com.example.smartshop.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

sealed class NavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    object Home : NavItem("home", "Home", Icons.Default.Home)
    object Products : NavItem("products", "Products", Icons.Default.ShoppingCart)
    object Profile : NavItem("profile", "Profile", Icons.Default.Person)
}

val navItems = listOf(
    NavItem.Home,
    NavItem.Products,
    NavItem.Profile
)