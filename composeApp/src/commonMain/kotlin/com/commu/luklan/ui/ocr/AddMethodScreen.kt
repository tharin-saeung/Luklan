package com.commu.luklan.ui.ocr

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.commu.luklan.ui.theme.LuklanTheme.LuklanTypography
import com.commu.luklan.ui.theme.LuklanSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMethodScreen(
    onManual: () -> Unit,
    onOcr: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "เพิ่มยา", style = LuklanTypography.h3) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBackIosNew, contentDescription = "Back") }
                }
            )
        },
        containerColor = androidx.compose.ui.graphics.Color.White
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = LuklanSpacing.lg),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "เลือกวิธีการเพิ่มยา", style = LuklanTypography.h3)
            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onOcr,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("OCR (สแกนฉลากยา)")
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onManual,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = androidx.compose.ui.graphics.Color.LightGray)
            ) {
                Text("เพิ่มด้วยตนเอง")
            }
        }
    }
}
