package com.commu.luklan.platform

import androidx.compose.runtime.*
import kotlinx.coroutines.launch

/**
 * Platform-specific image picker. Returns image bytes or null if not available.
 * Provide actual implementations in androidMain / iosMain. Stubs are provided to compile.
 */
enum class ImageSource {
    CAMERA, GALLERY
}

expect suspend fun pickImageFromDevice(source: ImageSource): ByteArray?

class ImagePickerLauncher(private val onImageSelected: (ByteArray?) -> Unit, private val scope: kotlinx.coroutines.CoroutineScope) {
    fun launch(source: ImageSource = ImageSource.GALLERY) {
        scope.launch {
            val result = pickImageFromDevice(source)
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
