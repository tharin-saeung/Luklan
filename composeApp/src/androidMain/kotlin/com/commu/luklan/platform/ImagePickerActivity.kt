package com.commu.luklan.platform

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import java.io.ByteArrayOutputStream

class ImagePickerActivity : ComponentActivity() {

    private val getImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            try {
                val inputStream = contentResolver.openInputStream(uri)
                val originalBitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()
                processAndComplete(originalBitmap)
            } catch (e: Exception) {
                android.util.Log.e("ImagePicker", "Error processing gallery image: ${e.message}")
                ImagePickerHolder.deferred?.complete(null)
                finish()
            }
        } else {
            ImagePickerHolder.deferred?.complete(null)
            finish()
        }
    }

    private val takePhoto = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        if (bitmap != null) {
            processAndComplete(bitmap)
        } else {
            ImagePickerHolder.deferred?.complete(null)
            finish()
        }
    }

    private fun processAndComplete(originalBitmap: Bitmap?) {
        try {
            if (originalBitmap != null) {
                // 1. Crop to Square
                val width = originalBitmap.width
                val height = originalBitmap.height
                val newSize = if (width > height) height else width
                val xOffset = (width - newSize) / 2
                val yOffset = (height - newSize) / 2
                
                val squareBitmap = Bitmap.createBitmap(originalBitmap, xOffset, yOffset, newSize, newSize)
                
                // 2. Scale down (e.g., 512x512)
                val scaledBitmap = Bitmap.createScaledBitmap(squareBitmap, 512, 512, true)
                
                // 3. Compress to JPEG
                val out = ByteArrayOutputStream()
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, out)
                val bytes = out.toByteArray()
                
                ImagePickerHolder.deferred?.complete(bytes)
                
                // Cleanup
                if (originalBitmap != squareBitmap) originalBitmap.recycle()
                squareBitmap.recycle()
                scaledBitmap.recycle()
            } else {
                ImagePickerHolder.deferred?.complete(null)
            }
        } catch (e: Exception) {
            android.util.Log.e("ImagePicker", "Error processing image: ${e.message}")
            ImagePickerHolder.deferred?.complete(null)
        } finally {
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val source = intent.getStringExtra("source") ?: ImageSource.GALLERY.name
        
        if (source == ImageSource.CAMERA.name) {
            takePhoto.launch(null)
        } else {
            getImage.launch("image/*")
        }
    }

    override fun onDestroy() {
        ImagePickerHolder.deferred?.complete(null)
        super.onDestroy()
    }
}
