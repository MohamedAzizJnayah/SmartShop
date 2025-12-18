package com.example.smartshop.ui.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.smartshop.ui.screens.auth.LoginScreen
import com.example.smartshop.ui.screens.auth.RegisterScreen
import com.example.smartshop.ui.screens.home.HomeScreen
import com.example.smartshop.ui.screens.dashbord.DashbordScreen
import com.example.smartshop.ui.viewmodel.authentication.AuthViewModel
import com.example.smartshop.ui.viewmodel.product.ProductViewModel

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object Dashboard : Screen("dashboard")   // ✅ NEW
}

@Composable
fun AppNav(navController: NavHostController) {

    val authVm: AuthViewModel = hiltViewModel()
    val productVm: ProductViewModel = hiltViewModel()

    NavHost(navController, startDestination = Screen.Login.route) {

        composable(Screen.Login.route) {
            LoginScreen(
                onSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                viewModel = authVm
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onNavigateToLogin = { navController.navigate(Screen.Login.route) },
                viewModel = authVm
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onGoToDashboard = {   // ✅ NEW
                    navController.navigate(Screen.Dashboard.route) {
                        launchSingleTop = true
                    }
                },
                authViewModel = authVm,
                productViewModel = productVm
            )
        }

        // ✅ NEW SCREEN
        composable(Screen.Dashboard.route) {
            DashbordScreen(
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                authViewModel = authVm,
                productViewModel = productVm
            )
        }
    }
}
