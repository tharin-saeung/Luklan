package com.commu.luklan.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.dp

object LuklanDimensions {
    // Button Heights (เพิ่มขนาดสำหรับผู้สูงอายุ)
    val buttonSmall = 44.dp       // เพิ่ม
    val buttonMedium = 52.dp      // เพิ่ม
    val buttonLarge = 60.dp       // เพิ่มจาก 56dp
    val buttonCircle = 72.dp

    // Touch Target (ตาม Material Design)
    val minTouchTarget = 48.dp    // เพิ่ม - ขนาดต่ำสุดที่กดได้

    // Icon Sizes
    val iconSmall = 20.dp         // เพิ่ม
    val iconMedium = 24.dp        // เพิ่ม
    val iconLarge = 32.dp         // เพิ่ม
    val iconXLarge = 48.dp        // เพิ่ม

    // Card/Container
    val cardElevation = 4.dp      // เพิ่ม
    val imageContainer = 240.dp
    val medicineIcon = 56.dp      // เพิ่ม - สำหรับไอคอนยา

    // Radius
    val radiusSmall = 12.dp
    val radiusMedium = 20.dp
    val radiusLarge = 28.dp
    val radiusCircle = 999.dp     // เพิ่ม - สำหรับปุ่มกลม

    // Indicators
    val indicatorSize = 10.dp     // เพิ่มจาก 8dp
    val indicatorSpacing = 8.dp   // เพิ่ม
}
// Create variable to call from Theme.kt
val LocalLuklanDimensions = staticCompositionLocalOf<LuklanDimensions> {
    error("No LuklanDimensions provided")
}