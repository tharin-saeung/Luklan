package com.commu.luklan.ui.main

import androidx.compose.foundation.background
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

data class TabItem(
    val tab: MainTab,
    val title: String,
    val icon: ImageVector
)

@Composable
fun MainScreen(
    onNavigateToAddMedicine: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToMedicineDetail: (Medicine) -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToMedicineGroups: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(MainTab.HOME) }
    
    val tabs = listOf(
        TabItem(MainTab.HOME, "หน้าหลัก", Icons.Outlined.Home),
        TabItem(MainTab.EMERGENCY, "ฉุกเฉิน", Icons.Default.Add),
        TabItem(MainTab.MENU, "เมนู", Icons.Default.Apps)
    )

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = LuklanColors.Background,
                tonalElevation = 0.dp
            ) {
                NavigationBarItem(
                    icon = {
                        Icon(
                            Icons.Outlined.Home,
                            contentDescription = "หน้าหลัก",
                            modifier = Modifier.size(28.dp)
                        )
                    },
                    label = {
                        Text(
                            text = "หน้าหลัก",
                            style = LuklanTypography.bodySmall,
                            fontWeight = if (selectedTab == MainTab.HOME) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    selected = selectedTab == MainTab.HOME,
                    onClick = { selectedTab = MainTab.HOME },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = LuklanColors.Primary,
                        selectedTextColor = LuklanColors.Primary,
                        unselectedIconColor = Color(0xFF9E9E9E),
                        unselectedTextColor = Color(0xFF9E9E9E),
                        indicatorColor = LuklanColors.Primary.copy(alpha = 0.1f)
                    )
                )
                
                NavigationBarItem(
                    icon = {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "ฉุกเฉิน",
                            modifier = Modifier.size(28.dp)
                        )
                    },
                    label = {
                        Text(
                            text = "ฉุกเฉิน",
                            style = LuklanTypography.bodySmall,
                            fontWeight = if (selectedTab == MainTab.EMERGENCY) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    selected = selectedTab == MainTab.EMERGENCY,
                    onClick = { selectedTab = MainTab.EMERGENCY },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFFE5A836),
                        selectedTextColor = Color(0xFFE5A836),
                        unselectedIconColor = Color(0xFF9E9E9E),
                        unselectedTextColor = Color(0xFF9E9E9E),
                        indicatorColor = Color(0xFFE5A836).copy(alpha = 0.1f)
                    )
                )
                
                NavigationBarItem(
                    icon = {
                        Icon(
                            Icons.Default.Apps,
                            contentDescription = "เมนู",
                            modifier = Modifier.size(28.dp)
                        )
                    },
                    label = {
                        Text(
                            text = "เมนู",
                            style = LuklanTypography.bodySmall,
                            fontWeight = if (selectedTab == MainTab.MENU) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    selected = selectedTab == MainTab.MENU,
                    onClick = { selectedTab = MainTab.MENU },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = LuklanColors.Primary,
                        selectedTextColor = LuklanColors.Primary,
                        unselectedIconColor = Color(0xFF9E9E9E),
                        unselectedTextColor = Color(0xFF9E9E9E),
                        indicatorColor = LuklanColors.Primary.copy(alpha = 0.1f)
                    )
                )
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
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        Column(
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
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
        // Header with "เมนู" title
        Text(
            text = "เมนู",
            style = LuklanTypography.h2,
            fontWeight = FontWeight.Bold,
            color = LuklanColors.Primary,
            modifier = Modifier.padding(horizontal = LuklanSpacing.lg, vertical = LuklanSpacing.lg)
        )
        
        // Search Bar
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
        
        // Large Cards Row (Medicine Groups)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .padding(horizontal = LuklanSpacing.lg, vertical = LuklanSpacing.sm),
            colors = CardDefaults.cardColors(containerColor = LuklanColors.Primary),
            shape = RoundedCornerShape(16.dp),
            onClick = onNavigateToMedicineGroups
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.Medication,
                        contentDescription = "Medicine Groups",
                        tint = Color.White,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "กลุ่มยา",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        
        // History Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .padding(horizontal = LuklanSpacing.lg, vertical = LuklanSpacing.sm),
            colors = CardDefaults.cardColors(containerColor = LuklanColors.Primary),
            shape = RoundedCornerShape(16.dp),
            onClick = onNavigateToHistory
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.History,
                        contentDescription = "History",
                        tint = Color.White,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "ประวัติ",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "การกินยา",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(LuklanSpacing.sm))
        
        // Small Cards Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = LuklanSpacing.lg),
            horizontalArrangement = Arrangement.spacedBy(LuklanSpacing.md)
        ) {
            // Caregiver Card
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(140.dp),
                colors = CardDefaults.cardColors(containerColor = LuklanColors.Primary),
                shape = RoundedCornerShape(16.dp),
                onClick = onNavigateToProfile
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = "Caregiver",
                            tint = Color.White,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "กลุ่มผู้ดูแล",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            // Members Card
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(140.dp),
                colors = CardDefaults.cardColors(containerColor = LuklanColors.Primary),
                shape = RoundedCornerShape(16.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.Group,
                            contentDescription = "Members",
                            tint = Color.White,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "สมาชิก",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
