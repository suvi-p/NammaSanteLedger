package com.example.nammasanthe.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Brand Colors
val PurpleStart = Color(0xFF7B2FBE)
val PurpleEnd = Color(0xFF4A90D9)
val PurpleDark = Color(0xFF6A0DAD)
val BlueAccent = Color(0xFF4A90D9)
val RedCredit = Color(0xFFE53935)
val GreenPayment = Color(0xFF43A047)
val OrangePending = Color(0xFFFF6D00)
val BackgroundLight = Color(0xFFF5F5F5)
val CardWhite = Color(0xFFFFFFFF)
val TextPrimary = Color(0xFF212121)
val TextSecondary = Color(0xFF757575)
val DividerColor = Color(0xFFE0E0E0)

private val LightColorScheme = lightColorScheme(
    primary = PurpleStart,
    secondary = BlueAccent,
    background = BackgroundLight,
    surface = CardWhite,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    error = RedCredit
)

@Composable
fun NammaSantheTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        content = content
    )
}
