package com.commu.luklan.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun rememberPlatformContext(): Any {
    return LocalContext.current
}
