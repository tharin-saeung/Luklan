package com.commu.luklan.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.commu.luklan.platform.ImageSource
import com.commu.luklan.ui.theme.LuklanColors
import com.commu.luklan.ui.theme.LuklanTheme.LuklanTypography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageSourceOptionDialog(
    onDismissRequest: () -> Unit,
    onSourceSelected: (ImageSource) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp, start = 24.dp, end = 24.dp)
        ) {
            Text(
                text = "เลือกรูปภาพ",
                style = LuklanTypography.h3,
                fontWeight = FontWeight.Bold,
                color = LuklanColors.Primary,
                modifier = Modifier.padding(vertical = 16.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SourceButton(
                    modifier = Modifier.weight(1f),
                    title = "กล้อง",
                    icon = Icons.Default.CameraAlt,
                    onClick = {
                        onSourceSelected(ImageSource.CAMERA)
                        onDismissRequest()
                    }
                )
                
                SourceButton(
                    modifier = Modifier.weight(1f),
                    title = "คลังภาพ",
                    icon = Icons.Default.PhotoLibrary,
                    onClick = {
                        onSourceSelected(ImageSource.GALLERY)
                        onDismissRequest()
                    }
                )
            }
        }
    }
}

@Composable
private fun SourceButton(
    modifier: Modifier = Modifier,
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    OutlinedCard(
        onClick = onClick,
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = Color.White),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color.LightGray))
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = LuklanColors.Primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = LuklanTypography.bodyMedium,
                color = LuklanColors.TextPrimary
            )
        }
    }
}
