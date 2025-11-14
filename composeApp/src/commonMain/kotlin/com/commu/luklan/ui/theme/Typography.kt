package com.commu.luklan.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

object LuklanTypography {
    // Headers
    val h1 = TextStyle(
        fontSize = 48.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 56.sp
    )
    
    val h3 = TextStyle(
        fontSize = 28.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 32.sp
    )
    
    // Body
    val bodyLarge = TextStyle(
        fontSize = 18.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 24.sp
    )
    
    val bodyMedium = TextStyle(
        fontSize = 16.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 20.sp
    )
    
    // Button
    val buttonLarge = TextStyle(
        fontSize = 16.sp,
        fontWeight = FontWeight.Medium
    )
    
    // Navigation Icon
    val navigationIcon = TextStyle(
        fontSize = 36.sp,
        fontWeight = FontWeight.Bold
    )
}