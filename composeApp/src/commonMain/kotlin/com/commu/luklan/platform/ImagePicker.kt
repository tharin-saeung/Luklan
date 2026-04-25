package com.commu.luklan.platform

import androidx.compose.runtime.*
import kotlinx.coroutines.launch

/**
 * Platform-specific image picker. Returns image bytes or null if not available.
 * Provide actual implementations in androidMain / iosMain. Stubs are provided to compile.
 */
expect suspend fun pickImageFromDevice(): ByteArray?

class ImagePickerLauncher(private val onImageSelected: (ByteArray?) -> Unit, private val scope: kotlinx.coroutines.CoroutineScope) {
    fun launch() {
        scope.launch {
            val result = pickImageFromDevice()
            onImageSelected(result)
        }
    }
}

@Composable
fun rememberImagePickerLauncher(onImageSelected: (ByteArray?) -> Unit): ImagePickerLauncher {
    val scope = rememberCoroutineScope()
    return remember(onImageSelected, scope) {
        ImagePickerLauncher(onImageSelected, scope)
    }
}
