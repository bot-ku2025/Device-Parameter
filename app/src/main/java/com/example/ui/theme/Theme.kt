package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val SentinelColorScheme = darkColorScheme(
    primary = SentinelCyan,
    onPrimary = Color.Black,
    primaryContainer = SentinelBlue,
    onPrimaryContainer = Color.White,
    secondary = SentinelBlueLight,
    onSecondary = Color.Black,
    secondaryContainer = SentinelSurfaceVariant,
    onSecondaryContainer = TextPrimary,
    tertiary = SentinelIndigo,
    background = SentinelDarkBg,
    onBackground = TextPrimary,
    surface = SentinelSurface,
    onSurface = TextPrimary,
    surfaceVariant = SentinelSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = SentinelCardBorder,
    error = StatusFail,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to Sentinel cyber dark mode
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = SentinelColorScheme,
        typography = Typography,
        content = content
    )
}

