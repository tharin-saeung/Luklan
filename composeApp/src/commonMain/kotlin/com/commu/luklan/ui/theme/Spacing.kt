package com.commu.luklan.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.dp

object LuklanSpacing {
    val xxs = 2.dp      // เพิ่มขนาดเล็กสุด
    val xs = 4.dp
    val sm = 8.dp
    val md = 16.dp
    val lg = 24.dp
    val xl = 32.dp
    val xxl = 48.dp
    val xxxl = 64.dp    // เพิ่มขนาดใหญ่สุด
}
// Create variable to call from Theme.kt
val LocalLuklanSpacing = staticCompositionLocalOf<LuklanSpacing> {
    error("No LuklanSpacing provided")
}