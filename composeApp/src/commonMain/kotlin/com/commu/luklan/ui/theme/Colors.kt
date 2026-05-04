package com.commu.luklan.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

object LuklanColors {
    // Primary (Blue)
    val Primary = Color(0xFF33658A)
    val PrimaryDark = Color(0xFF2E4857)
    val OnPrimary = Color(0xFFFFFFFF)

    // Secondary (Yellow/Orange)
    val Secondary = Color(0xFFF7AE2C)
    val OnSecondary = Color(0xFFFFFFFF)

    // Tertiary (Green)
    val Success = Color(0xFF4B9C1F)
    val Tertiary = Color(0xFF4B9C1F)

    // Error (Red)
    val Error = Color(0xFFE60000)

    // Background & Surface
    val Background = Color(0xFFEEFDFF)
    val Surface = Color(0xFFFFFFFF)
    val SurfaceVariant = Color(0xFFE8EEF2)

    // Text
    val TextPrimary = Color(0xFF2E4857)
    val TextSecondary = Color(0xFF7F8C8D)
    val TextOnPrimary = Color(0xFFFFFFFF)

    // UI Elements
    val Indicator = Color(0xFFE0E0E0)
    val IndicatorActive = Color(0xFFF7AE2C)
}
// Create variable to call from Theme.kt
val LocalLuklanColors = staticCompositionLocalOf<LuklanColors> {
    error("No LuklanColors provided")
}