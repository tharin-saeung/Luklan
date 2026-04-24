package com.commu.luklan.ui.contact

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.commu.luklan.ui.theme.LuklanColors
import com.commu.luklan.ui.theme.LuklanTypography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ติดต่อทีมงาน", style = LuklanTypography.h3, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = LuklanColors.Primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = LuklanColors.Background)
            )
        },
        containerColor = LuklanColors.Background
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(16.dp))
            
            Text(
                "ทีมงาน Luklan (ลูกหลาน)",
                style = LuklanTypography.h2,
                color = LuklanColors.Primary,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                "เราพร้อมช่วยเหลือคุณเสมอ",
                style = LuklanTypography.bodyLarge,
                color = LuklanColors.TextSecondary
            )

            Spacer(Modifier.height(32.dp))

            ContactCard(
                icon = Icons.Default.Phone,
                label = "เบอร์โทรศัพท์",
                value = "02-123-4567"
            )

            ContactCard(
                icon = Icons.Default.Email,
                label = "อีเมล",
                value = "support@luklan.com"
            )

            ContactCard(
                icon = Icons.Default.Language,
                label = "เว็บไซต์",
                value = "www.luklan.com"
            )

            Spacer(Modifier.weight(1f))

            Text(
                "เวอร์ชัน 1.0.0",
                style = LuklanTypography.bodySmall,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun ContactCard(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(48.dp).clip(CircleShape).background(LuklanColors.Primary.copy(0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = LuklanColors.Primary)
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(label, style = LuklanTypography.bodySmall, color = Color.Gray)
                Text(value, style = LuklanTypography.bodyLarge, fontWeight = FontWeight.Bold, color = LuklanColors.Primary)
            }
        }
    }
}
