package com.commu.luklan.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.commu.luklan.platform.ImageSource
import com.commu.luklan.platform.rememberImagePickerLauncher
import com.commu.luklan.ui.theme.LuklanColors
import com.commu.luklan.ui.theme.LuklanTheme.LuklanTypography

@Composable
fun ImageSelector(
    image: Any?, // Can be String (URL) or ByteArray
    isUploading: Boolean = false,
    onImageSelected: (ByteArray?) -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 140.dp,
    placeholder: @Composable () -> Unit = {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.PhotoCamera, null, tint = Color.White, modifier = Modifier.size(size * 0.28f))
            Text("เพิ่มรูปถ่าย", color = Color.White, style = LuklanTypography.caption)
        }
    }
) {
    var showSourceDialog by remember { mutableStateOf(false) }
    val imagePickerLauncher = rememberImagePickerLauncher(onImageSelected = onImageSelected)

    val hasImage = remember(image) {
        when (image) {
            is String -> image.isNotEmpty()
            is ByteArray -> image.isNotEmpty()
            else -> image != null
        }
    }

    Box(
        modifier = modifier
            .size(size)
            .clickable { showSourceDialog = true },
        contentAlignment = Alignment.Center
    ) {
        // Main Circle Content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.2f))
                .border(2.dp, if (hasImage) Color.Transparent else Color.White, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (hasImage) {
                AsyncImage(
                    model = image,
                    contentDescription = "Selected Photo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else if (isUploading) {
                CircularProgressIndicator(color = Color.White)
            } else {
                placeholder()
            }
        }

        // Camera Overlay (Yellow Icon)
        if (hasImage && !isUploading) {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(size * 0.28f),
                shape = CircleShape,
                color = LuklanColors.Secondary,
                border = BorderStroke(1.5f.dp, Color.White),
                shadowElevation = 2.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.PhotoCamera,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(size * 0.14f)
                    )
                }
            }
        }
    }

    if (showSourceDialog) {
        ImageSourceOptionDialog(
            onDismissRequest = { showSourceDialog = false },
            onSourceSelected = { source ->
                imagePickerLauncher.launch(source)
            }
        )
    }
}
