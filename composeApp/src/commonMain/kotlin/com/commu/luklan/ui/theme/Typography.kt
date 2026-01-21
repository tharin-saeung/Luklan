package com.commu.luklan.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

object LuklanTypography {
    // Headers (เพิ่มขนาดสำหรับผู้สูงอายุ)
    val h1 = TextStyle(
        fontSize = 56.sp,           // เพิ่มจาก 48sp
        fontWeight = FontWeight.Bold, 
        lineHeight = 64.sp
    )

    val h2 = TextStyle(
        fontSize = 36.sp,           // เพิ่มจาก 32sp
        fontWeight = FontWeight.Bold, 
        lineHeight = 44.sp
    )

    val h3 = TextStyle(
        fontSize = 24.sp,           // ลดจาก 28sp (สำหรับ title)
        fontWeight = FontWeight.Bold, 
        lineHeight = 32.sp
    )
    
    val h4 = TextStyle(
        fontSize = 20.sp,           // เพิ่มขนาดใหม่
        fontWeight = FontWeight.SemiBold, 
        lineHeight = 28.sp
    )

    // Body (เพิ่มขนาดทุกระดับ)
    val bodyLarge = TextStyle(
        fontSize = 20.sp,           // เพิ่มจาก 18sp
        fontWeight = FontWeight.Normal, 
        lineHeight = 28.sp
    )

    val bodyMedium = TextStyle(
        fontSize = 18.sp,           // เพิ่มจาก 16sp
        fontWeight = FontWeight.Normal, 
        lineHeight = 26.sp
    )

    val bodySmall = TextStyle(
        fontSize = 16.sp,           // เพิ่มจาก 14sp
        fontWeight = FontWeight.Normal, 
        lineHeight = 22.sp
    )
    
    val caption = TextStyle(
        fontSize = 14.sp,           // สำหรับข้อความเล็กๆ
        fontWeight = FontWeight.Normal, 
        lineHeight = 20.sp
    )

    // Button (เพิ่มขนาด)
    val buttonLarge = TextStyle(
        fontSize = 18.sp,           // เพิ่มจาก 16sp
        fontWeight = FontWeight.SemiBold,
        lineHeight = 24.sp
    )
    
    val buttonMedium = TextStyle(
        fontSize = 16.sp,
        fontWeight = FontWeight.Medium,
        lineHeight = 22.sp
    )

    // Navigation Icon
    val navigationIcon = TextStyle(
        fontSize = 40.sp,           // เพิ่มจาก 36sp
        fontWeight = FontWeight.Bold
    )
}