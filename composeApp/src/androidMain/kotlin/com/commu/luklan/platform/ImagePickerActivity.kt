package com.commu.luklan.platform

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts

class ImagePickerActivity : ComponentActivity() {

    private val getImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        try {
            if (uri != null) {
                val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() }
                ImagePickerHolder.deferred?.complete(bytes)
            } else {
                ImagePickerHolder.deferred?.complete(null)
            }
        } catch (e: Exception) {
            ImagePickerHolder.deferred?.complete(null)
        } finally {
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Launch the system picker for images
        getImage.launch("image/*")
    }

    override fun onDestroy() {
        ImagePickerHolder.deferred?.complete(null)
        super.onDestroy()
    }
}
