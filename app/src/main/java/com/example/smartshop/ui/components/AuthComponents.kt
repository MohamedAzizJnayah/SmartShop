package com.example.smartshop.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.FocusInteraction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.smartshop.ui.theme.PrimaryGradientEnd
import com.example.smartshop.ui.theme.PrimaryGradientStart

// 1. ANIMATED GRADIENT BUTTON
@Composable
fun AnimatedGradientButton(
    text: String,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .shadow(8.dp, RoundedCornerShape(12.dp)),
        enabled = !isLoading,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent
        ),
        contentPadding = PaddingValues(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(PrimaryGradientStart, PrimaryGradientEnd)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Text(
                    text,
                    color = Color.White,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

// 2. ANIMATED TEXT FIELD WITH FOCUS EFFECT
@Composable
fun AnimatedAuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isPassword: Boolean = false,
    modifier: Modifier = Modifier,
    isError: Boolean = false
) {
    var isFocused by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }

    val animatedBorderColor by animateColorAsState(
        targetValue = if (isFocused) PrimaryGradientStart else Color.Gray,
        animationSpec = tween(300),
        label = "BorderColor"
    )
    val animatedElevation by animateDpAsState(
        targetValue = if (isFocused) 8.dp else 0.dp,
        animationSpec = spring(),
        label = "Elevation"
    )

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier
            .shadow(animatedElevation, RoundedCornerShape(12.dp))
            .border(
                width = if (isFocused) 2.dp else 1.dp,
                color = if (isError) Color.Red else animatedBorderColor,
                shape = RoundedCornerShape(12.dp)
            ),
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        isError = isError,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent
        ),
        interactionSource = interactionSource
    )

    LaunchedEffect(Unit) {
        interactionSource.interactions.collect { interaction ->
            isFocused = when (interaction) {
                is FocusInteraction.Focus -> true
                is FocusInteraction.Unfocus -> false
                else -> isFocused
            }
        }
    }
}

// 3. ANIMATED ERROR MESSAGE
@Composable
fun AnimatedErrorMessage(errorMessage: String?, modifier: Modifier = Modifier) {
    AnimatedVisibility(
        visible = errorMessage != null,
        enter = slideInVertically() + fadeIn(),
        exit = slideOutVertically() + fadeOut(),
        modifier = modifier,
        label = "ErrorMessage"
    ) {
        errorMessage?.let {
            Surface(
                color = Color(0xFFFFEBEE),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(2.dp, RoundedCornerShape(8.dp))
            ) {
                Text(
                    text = it,
                    color = Color(0xFFC62828),
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
    }
}

// 4. ANIMATED DIVIDER WITH TEXT
@Composable
fun AnimatedDividerWithText(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f))
        Text(
            text,
            modifier = Modifier.padding(horizontal = 12.dp),
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray
        )
        HorizontalDivider(modifier = Modifier.weight(1f))
    }
}
// 5. ANIMATED SUCCESS CHECKMARK
@Composable
fun AnimatedSuccessCheckmark(visible: Boolean) {
    AnimatedVisibility(
        visible = visible,
        enter = scaleIn() + fadeIn(),
        exit = scaleOut() + fadeOut(),
        label = "SuccessCheckmark"
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(48.dp)
                .background(
                    color = Color(0xFF10B981),
                    shape = RoundedCornerShape(50.dp)
                )
        ) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = "Success",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}