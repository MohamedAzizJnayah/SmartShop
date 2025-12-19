package com.example.smartshop.ui.screens.auth

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.smartshop.ui.components.AnimatedAuthTextField
import com.example.smartshop.ui.components.AnimatedErrorMessage
import com.example.smartshop.ui.components.AnimatedGradientButton
import com.example.smartshop.ui.theme.PrimaryGradientEnd
import com.example.smartshop.ui.theme.PrimaryGradientStart
import com.example.smartshop.ui.viewmodel.authentication.AuthViewModel
import kotlinx.coroutines.delay

@Composable
fun LoginScreen(
    onSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()
    val user by viewModel.user.collectAsState()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showSuccess by remember { mutableStateOf(false) }

    // Animation trigger for entrance
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isVisible = true }

    LaunchedEffect(user) {
        if (user != null) {
            showSuccess = true
            // Delay navigation for smooth animation
            delay(800)
            onSuccess()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F172A),
                        Color(0xFF1E293B)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // ANIMATED HEADER
            AnimatedVisibility(
                visible = isVisible && !showSuccess,
                enter = slideInVertically(initialOffsetY = { -100 }) + fadeIn(),
                exit = slideOutVertically() + fadeOut(),
                label = "Header"
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Animated Logo Circle
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(PrimaryGradientStart, PrimaryGradientEnd)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "S",
                            color = Color.White,
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        "SmartShop",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        "Connectez-vous à votre compte",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFCBD5E1)
                    )

                    Spacer(modifier = Modifier.height(40.dp))
                }
            }

            // SUCCESS ANIMATION
            AnimatedVisibility(
                visible = showSuccess,
                enter = scaleIn() + fadeIn(),
                exit = scaleOut() + fadeOut(),
                label = "Success"
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(80.dp)
                            .background(
                                color = Color(0xFF10B981),
                                shape = RoundedCornerShape(50.dp)
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = "Success",
                            tint = Color.White,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Connexion réussie!",
                        color = Color(0xFF10B981),
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
            }

            // FORM INPUTS (animate in staggered)
            AnimatedVisibility(
                visible = isVisible && !showSuccess,
                enter = slideInVertically(initialOffsetY = { 100 }) + fadeIn(),
                label = "Form"
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // EMAIL FIELD
                    AnimatedAuthTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = "Adresse email",
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // PASSWORD FIELD
                    AnimatedAuthTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = "Mot de passe",
                        isPassword = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // ERROR MESSAGE
                    AnimatedErrorMessage(error)

                    Spacer(modifier = Modifier.height(24.dp))

                    // LOGIN BUTTON
                    AnimatedGradientButton(
                        text = "Se connecter",
                        isLoading = loading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        onClick = { viewModel.login(email, password) }
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // REGISTER LINK
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            "Pas encore inscrit? ",
                            color = Color(0xFFCBD5E1),
                            style = MaterialTheme.typography.bodySmall
                        )
                        TextButton(onClick = onNavigateToRegister) {
                            Text(
                                "Créer un compte",
                                color = PrimaryGradientStart,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}