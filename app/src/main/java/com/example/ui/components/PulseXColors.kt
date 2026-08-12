package com.example.ui.components

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

object PulseXColors {
    val Background = Color(0xFF0B1020)
    val Surface = Color(0xFF12182C)
    val GlassCardBg = Color(0xFF1B2440)
    val GlassCardBorder = Color(0x14FFFFFF) // 8% opacity white/silver border
    val Primary = Color(0xFF5B8CFF) // Premium Galaxy primary button / active accent
    val PrimaryContainer = Color(0xFF26345C) // Elevated Card / dark indigo slate
    val Secondary = Color(0xFFB8C2D9) // Secondary text color
    val SecondaryContainer = Color(0x267DD3FC) // Soft Glow (15% opacity #7DD3FC) for pressed state
    val OnSurface = Color(0xFFF8FAFC) // Primary Text
    val OnSurfaceVariant = Color(0xFFB8C2D9) // Secondary Text
    val Outline = Color(0xFF8A94AA) // Inactive Bottom Nav / secondary borders
    val PositiveGreen = Color(0xFF00D26A) // Bullish green
    val NegativeRed = Color(0xFFFF4D6D) // Bearish red

    val BlueGlow = Color(0xFF5B8CFF) // Focused border/shadow glow color
    val AiAccent = Color(0xFFA855F7) // Purple AI Accent for badges, icons and summaries

    val PrimaryGradient = Brush.linearGradient(
        colors = listOf(Color(0xFF5B8CFF), Color(0xFF4A7AF2))
    )

    val CardBorderGradient = Brush.linearGradient(
        colors = listOf(Color(0x14FFFFFF), Color(0x05FFFFFF))
    )
}


