package com.commu.luklan.ui.theme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

@Composable
fun LuklanTheme(content: @Composable () -> Unit) {
    val googlesans = getGoogleSansFamily()
    val colors = LuklanColors
    val typography = LuklanTypography(googlesans)
    val spacing = LuklanSpacing
    val dimensions = LuklanDimensions

    CompositionLocalProvider(
        LocalLuklanColors provides colors,
        LocalLuklanTypography provides typography,
        LocalLuklanSpacing provides spacing,
        LocalLuklanDimensions provides dimensions
    ) {
        val lightColorScheme = lightColorScheme(
            primary = colors.Primary,
            onPrimary = androidx.compose.ui.graphics.Color.White,
            secondary = colors.Secondary,
            onSecondary = androidx.compose.ui.graphics.Color.White,
            background = colors.Background,
            surface = androidx.compose.ui.graphics.Color.White,
            onSurface = colors.TextPrimary,
            surfaceVariant = colors.Background,
            onSurfaceVariant = colors.TextSecondary,
            surfaceContainer = androidx.compose.ui.graphics.Color.White,
            surfaceContainerHigh = androidx.compose.ui.graphics.Color.White,
            surfaceContainerLow = colors.Background,
            outline = colors.Primary.copy(alpha = 0.5f),
            outlineVariant = colors.Primary.copy(alpha = 0.2f)
        )

        MaterialTheme(
            colorScheme = lightColorScheme,
            typography = typography.toMaterial3(),
            content = content
        )
    }
}

object LuklanTheme {
    val LuklanColors: LuklanColors
        @Composable
        get() = LocalLuklanColors.current
    val LuklanTypography: LuklanTypography
        @Composable
        get() = LocalLuklanTypography.current
    val LuklanSpacing: LuklanSpacing
        @Composable
        get() = LocalLuklanSpacing.current
    val LuklanDimensions: LuklanDimensions
        @Composable
        get() = LocalLuklanDimensions.current
}
