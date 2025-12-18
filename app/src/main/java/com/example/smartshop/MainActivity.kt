package com.example.smartshop


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import com.example.smartshop.ui.navigation.AppNav
import com.example.smartshop.ui.theme.SmartShopTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            SmartShopApp()
        }
    }
}

@Composable
fun SmartShopApp() {
    SmartShopTheme {
        val navController = rememberNavController()
        AppNav(navController = navController)
    }
}
