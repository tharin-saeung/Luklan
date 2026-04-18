package com.commu.luklan.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.commu.luklan.data.Medicine
import com.commu.luklan.ui.home.HomeScreen
import com.commu.luklan.ui.theme.*

enum class MainTab {
    HOME,
    EMERGENCY,
    MENU
}

@Composable
fun MainScreen(
    onNavigateToAddMedicine: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToMedicineDetail: (Medicine) -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToMedicineGroups: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(MainTab.HOME) }

    Scaffold(
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                // Main Navigation background
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                    color = Color.White,
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 32.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Home Tab
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable { selectedTab = MainTab.HOME }
                        ) {
                            Icon(
                                imageVector = if (selectedTab == MainTab.HOME) Icons.Filled.Home else Icons.Outlined.Home,
                                contentDescription = "Home",
                                tint = LuklanColors.Primary,
                                modifier = Modifier.size(30.dp)
                            )
                            Text(
                                text = "หน้าหลัก",
                                style = LuklanTypography.bodySmall,
                                color = LuklanColors.Primary,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(Modifier.width(80.dp)) // Space for central emergency button

                        // Menu Tab
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable { selectedTab = MainTab.MENU }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Apps,
                                contentDescription = "Menu",
                                tint = LuklanColors.Primary,
                                modifier = Modifier.size(30.dp)
                            )
                            Text(
                                text = "เมนู",
                                style = LuklanTypography.bodySmall,
                                color = LuklanColors.Primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Central Emergency "Hump" Button
                Box(
                    modifier = Modifier
                        .size(width = 120.dp, height = 90.dp)
                        .offset(y = (-5).dp)
                        .clip(RoundedCornerShape(topStart = 60.dp, topEnd = 60.dp, bottomStart = 10.dp, bottomEnd = 10.dp))
                        .background(LuklanColors.Secondary)
                        .clickable { selectedTab = MainTab.EMERGENCY },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "ฉุกเฉิน",
                        color = Color.White,
                        style = LuklanTypography.h3,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        containerColor = LuklanColors.Background
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when (selectedTab) {
                MainTab.HOME -> HomeScreen(
                    onNavigateToAddMedicine = onNavigateToAddMedicine,
                    onNavigateToProfile = onNavigateToProfile,
                    onNavigateToMedicineDetail = onNavigateToMedicineDetail
                )
                MainTab.EMERGENCY -> EmergencyScreen()
                MainTab.MENU -> MenuScreen(
                    onNavigateToProfile = onNavigateToProfile,
                    onNavigateToHistory = onNavigateToHistory,
                    onNavigateToMedicineGroups = onNavigateToMedicineGroups
                )
            }
        }
    }
}

@Composable
fun EmergencyScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(LuklanSpacing.lg),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                Icons.Default.LocalHospital,
                contentDescription = "Emergency",
                tint = LuklanColors.Primary,
                modifier = Modifier.size(64.dp)
            )
            Text(
                text = "ฉุกเฉิน",
                style = LuklanTypography.h2,
                fontWeight = FontWeight.Bold,
                color = LuklanColors.TextPrimary
            )
            Text(
                text = "ฟีเจอร์นี้จะพร้อมใช้งานเร็วๆ นี้",
                style = LuklanTypography.bodyLarge,
                color = LuklanColors.TextSecondary
            )
        }
    }
}

@Composable
fun MenuScreen(
    onNavigateToProfile: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToMedicineGroups: () -> Unit
) {
    var searchText by remember { mutableStateOf("") }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LuklanColors.Background)
    ) {
        Text(
            text = "เมนู",
            style = LuklanTypography.h2,
            fontWeight = FontWeight.Bold,
            color = LuklanColors.Primary,
            modifier = Modifier.padding(horizontal = LuklanSpacing.lg, vertical = LuklanSpacing.lg)
        )
        
        OutlinedTextField(
            value = searchText,
            onValueChange = { searchText = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = LuklanSpacing.lg, vertical = LuklanSpacing.sm),
            placeholder = { Text("ค้นหา", color = Color(0xFF9E9E9E)) },
            leadingIcon = {
                Icon(
                    Icons.Default.Search,
                    contentDescription = "Search",
                    tint = LuklanColors.Primary
                )
            },
            shape = RoundedCornerShape(50.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = LuklanColors.Primary.copy(alpha = 0.3f),
                unfocusedBorderColor = Color(0xFFE0E0E0)
            ),
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(LuklanSpacing.md))
        
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .padding(horizontal = LuklanSpacing.lg, vertical = LuklanSpacing.sm),
            colors = CardDefaults.cardColors(containerColor = LuklanColors.Primary),
            shape = RoundedCornerShape(24.dp),
            onClick = onNavigateToMedicineGroups
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Medication, contentDescription = null, tint = Color.White, modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "กลุ่มยา", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .padding(horizontal = LuklanSpacing.lg, vertical = LuklanSpacing.sm),
            colors = CardDefaults.cardColors(containerColor = LuklanColors.Primary),
            shape = RoundedCornerShape(24.dp),
            onClick = onNavigateToHistory
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.History, contentDescription = null, tint = Color.White, modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "ประวัติการกินยา", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
