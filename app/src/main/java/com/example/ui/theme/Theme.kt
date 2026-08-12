package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val PulseXDarkColorScheme = darkColorScheme(
    primary = LightPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF1D2129),
    onPrimaryContainer = TextPrimary,
    secondary = AccentCyan,
    onSecondary = Color.White,
    background = DarkBackground,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = GlassCard,
    onSurfaceVariant = TextSecondary,
    outline = OutlineGrey
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = PulseXDarkColorScheme,
        typography = Typography,
        content = content
    )
}
