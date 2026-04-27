package com.commu.luklan.utils

import androidx.compose.runtime.Composable

@Composable
actual fun CommonBackHandler(enabled: Boolean, onBack: () -> Unit) {
    // No-op for iOS as it doesn't have a hardware back button
}
