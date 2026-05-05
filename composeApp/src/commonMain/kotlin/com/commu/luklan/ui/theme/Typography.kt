package com.commu.luklan.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import luklan.composeapp.generated.resources.Res
import luklan.composeapp.generated.resources.googlesans_bold
import luklan.composeapp.generated.resources.googlesans_italic
import luklan.composeapp.generated.resources.googlesans_medium
import luklan.composeapp.generated.resources.googlesans_regular
import luklan.composeapp.generated.resources.googlesans_semibold
import org.jetbrains.compose.resources.Font

@Composable
fun getGoogleSansFamily() = FontFamily(
    Font(Res.font.googlesans_regular, FontWeight.Normal),
    Font(Res.font.googlesans_italic, FontWeight.Normal, FontStyle.Italic),
    Font(Res.font.googlesans_medium, FontWeight.Medium),
    Font(Res.font.googlesans_semibold, FontWeight.SemiBold),
    Font(Res.font.googlesans_bold, FontWeight.Bold)
)

class LuklanTypography(val fontFamily: FontFamily) {
    val h1 = TextStyle(
        fontFamily = fontFamily,
        fontSize = 32.sp,
        fontWeight = FontWeight.Bold, 
        lineHeight = 38.sp
    )
    val h2 = TextStyle(
        fontFamily = fontFamily,
        fontSize = 26.sp,
        fontWeight = FontWeight.Bold, 
        lineHeight = 32.sp
    )
    val h3 = TextStyle(
        fontFamily = fontFamily,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold, 
        lineHeight = 26.sp
    )
    val h4 = TextStyle(
        fontFamily = fontFamily,
        fontSize = 18.sp,
        fontWeight = FontWeight.SemiBold, 
        lineHeight = 28.sp
    )
    val bodyLarge = TextStyle(
        fontFamily = fontFamily,
        fontSize = 20.sp,
        fontWeight = FontWeight.Normal, 
        lineHeight = 28.sp
    )
    val bodyMedium = TextStyle(
        fontFamily = fontFamily,
        fontSize = 18.sp,
        fontWeight = FontWeight.Normal, 
        lineHeight = 26.sp
    )
    val bodySmall = TextStyle(
        fontFamily = fontFamily,
        fontSize = 16.sp,
        fontWeight = FontWeight.Normal, 
        lineHeight = 22.sp
    )
    val caption = TextStyle(
        fontFamily = fontFamily,
        fontSize = 14.sp,
        fontWeight = FontWeight.Normal, 
        lineHeight = 20.sp
    )
    val buttonLarge = TextStyle(
        fontFamily = fontFamily,
        fontSize = 18.sp,
        fontWeight = FontWeight.SemiBold,
        lineHeight = 24.sp
    )
    val buttonMedium = TextStyle(
        fontFamily = fontFamily,
        fontSize = 16.sp,
        fontWeight = FontWeight.Medium,
        lineHeight = 22.sp
    )
    val navigationIcon = TextStyle(
        fontFamily = fontFamily,
        fontSize = 40.sp,
        fontWeight = FontWeight.Bold
    )

    // Convert to Material3 Typography
    fun toMaterial3() = androidx.compose.material3.Typography(
        displayLarge = h1,
        displayMedium = h2,
        displaySmall = h3,
        headlineLarge = h1,
        headlineMedium = h2,
        headlineSmall = h3,
        titleLarge = h3,
        titleMedium = h4,
        titleSmall = bodyLarge,
        bodyLarge = bodyLarge,
        bodyMedium = bodyMedium,
        bodySmall = bodySmall,
        labelLarge = buttonLarge,
        labelMedium = buttonMedium,
        labelSmall = caption
    )
}

val LocalLuklanTypography = staticCompositionLocalOf<LuklanTypography> {
    error("No LuklanTypography provided")
}
