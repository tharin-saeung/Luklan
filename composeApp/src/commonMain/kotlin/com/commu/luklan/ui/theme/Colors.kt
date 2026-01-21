package com.commu.luklan.ui.theme

import androidx.compose.ui.graphics.Color

object LuklanColors {
    // Primary (Blue)
    val Primary = Color(0xFF4A7C9E)          // น้ำเงินอ่อนกว่าเดิม
    val PrimaryDark = Color(0xFF3A6C8E)
    val OnPrimary = Color(0xFFFFFFFF)

    // Secondary (Yellow/Orange)
    val Secondary = Color(0xFFF59E42)        // ส้มตาม mockup
    val SecondaryYellow = Color(0xFFFDB93A)  // เหลืองสำหรับ indicator
    val OnSecondary = Color(0xFFFFFFFF)

    // Tertiary (Green)
    val Success = Color(0xFF4CAF50)
    val Tertiary = Color(0xFF4B9C1F)

    // Error (Red)
    val Error = Color(0xFFE74C3C)

    // Background & Surface
    val Background = Color(0xFFF5F8FA)       // ขาว-เทาอ่อนตาม mockup
    val Surface = Color(0xFFFFFFFF)
    val SurfaceVariant = Color(0xFFE8EEF2)   // สำหรับ card secondary

    // Text
    val TextPrimary = Color(0xFF2C3E50)      // น้ำเงินเข้มอ่านง่าย
    val TextSecondary = Color(0xFF7F8C8D)    // เทาอ่อน
    val TextOnPrimary = Color(0xFFFFFFFF)

    // UI Elements
    val Indicator = Color(0xFFE0E0E0)        // indicator ไม่ active
    val IndicatorActive = Color(0xFFF59E42)  // ส้มเมื่อ active
}
