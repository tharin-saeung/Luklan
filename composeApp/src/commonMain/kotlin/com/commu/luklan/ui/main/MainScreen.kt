package com.commu.luklan.ui.main

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.commu.luklan.data.Medicine
import com.commu.luklan.data.User
import com.commu.luklan.data.getAuthRepository
import com.commu.luklan.data.getAlertRepository
import com.commu.luklan.data.getNotificationScheduler
import com.commu.luklan.ui.home.HomeScreen
import com.commu.luklan.ui.theme.*
import com.commu.luklan.utils.getCurrentTimeMillis
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import luklan.composeapp.generated.resources.Res
import luklan.composeapp.generated.resources.capsule2
import org.jetbrains.compose.resources.painterResource

enum class MainTab {
    HOME,
    EMERGENCY,
    MENU
}

@Composable
fun MainScreen(
    selectedTab: MainTab = MainTab.HOME,
    onTabSelected: (MainTab) -> Unit,
    onNavigateToAddMedicine: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToMedicineDetail: (Medicine, String) -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToMedicineGroups: () -> Unit,
    onNavigateToInviteCaretaker: () -> Unit,
    onNavigateToCaretakerDashboard: () -> Unit,
    onNavigateToPatientTimeline: (String, String) -> Unit,
    onNavigateToNotificationCenter: (String) -> Unit
) {
    val authRepository = remember { getAuthRepository() }
    val alertRepository = remember { getAlertRepository() }
    val notificationScheduler = remember { getNotificationScheduler() }
    var userProfile by remember { mutableStateOf<User?>(null) }
    val scope = rememberCoroutineScope()

    var lastNotifiedAlertId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        authRepository.getCurrentUserId()?.let { uid ->
            authRepository.getUserProfile(uid).onSuccess { 
                userProfile = it
                scope.launch {
                    println("🔔 Starting alert polling for $uid")
                    while(true) {
                        alertRepository.getAlertsForUser(uid).onSuccess { alerts ->
                            val latest = alerts.firstOrNull()
                            if (latest != null && latest.id != lastNotifiedAlertId && latest.timestamp > (getCurrentTimeMillis() - 120000)) {
                                if (latest.senderId != uid) {
                                    println("🆘 Triggering local notif for SOS: ${latest.id}")
                                    notificationScheduler.showImmediateNotification(
                                        "🆘 SOS จาก ${latest.senderName}",
                                        "${latest.senderName} ต้องการความช่วยเหลือด่วน!!"
                                    )
                                    lastNotifiedAlertId = latest.id
                                }
                            }
                        }
                        delay(15000) // Poll every 15s
                    }
                }
            }
        }
    }

    Scaffold(
        bottomBar = {
            Box(
                modifier = Modifier.fillMaxWidth().height(110.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth().height(80.dp),
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                    color = Color.White,
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TabItem(
                            icon = Icons.Filled.Home,
                            label = "หน้าหลัก",
                            isSelected = selectedTab == MainTab.HOME,
                            onClick = { onTabSelected(MainTab.HOME) },
                            modifier = Modifier.weight(1f)
                        )

                        Box(modifier = Modifier.weight(1.25f)) // Spacer for center button

                        TabItem(
                            icon = Icons.Default.Apps,
                            label = "เมนู",
                            isSelected = selectedTab == MainTab.MENU,
                            onClick = { onTabSelected(MainTab.MENU) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                
                EmergencyButton(
                    onTrigger = { onTabSelected(MainTab.EMERGENCY) },
                    modifier = Modifier.width(150.dp).height(110.dp)
                )
            }
        },
        containerColor = LuklanColors.Background
    ) { _ ->
        Box(modifier = Modifier.fillMaxSize().padding(bottom = 80.dp)) {
            when (selectedTab) {
                MainTab.HOME -> {
                    HomeScreen(
                        onNavigateToAddMedicine = onNavigateToAddMedicine,
                        onNavigateToProfile = onNavigateToProfile,
                        onNavigateToMedicineDetail = onNavigateToMedicineDetail,
                        onNavigateToNotificationCenter = onNavigateToNotificationCenter
                    )
                }
                MainTab.EMERGENCY -> EmergencyScreen(
                    userProfile = userProfile,
                    onBack = { onTabSelected(MainTab.HOME) }
                )
                MainTab.MENU -> MenuScreen(
                    userRole = userProfile?.role,
                    onNavigateToProfile = onNavigateToProfile,
                    onNavigateToHistory = onNavigateToHistory,
                    onNavigateToMedicineGroups = onNavigateToMedicineGroups,
                    onNavigateToInviteCaretaker = onNavigateToInviteCaretaker,
                    onNavigateToCaretakerDashboard = onNavigateToCaretakerDashboard,
                    onNavigateToNotificationCenter = onNavigateToNotificationCenter
                )
            }
        }
    }
}

@Composable
fun TabItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector, 
    label: String, 
    isSelected: Boolean, 
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .fillMaxHeight()
            .clickable { onClick() }
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) LuklanColors.Primary else LuklanColors.Primary.copy(alpha = 0.5f),
            modifier = Modifier.size(30.dp)
        )
        Text(
            text = label,
            style = LuklanTypography.bodySmall,
            color = if (isSelected) LuklanColors.Primary else LuklanColors.Primary.copy(alpha = 0.5f),
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun EmergencyButton(onTrigger: () -> Unit, modifier: Modifier = Modifier) {
    var isHolding by remember { mutableStateOf(false) }
    val progress by animateFloatAsState(
        targetValue = if (isHolding) 1f else 0f,
        animationSpec = if (isHolding) tween(2000, easing = LinearEasing) else tween(300),
        label = "SOSProgress"
    )

    LaunchedEffect(progress) {
        if (progress == 1f) {
            onTrigger()
            isHolding = false
        }
    }

    val bellShape = GenericShape { size, _ ->
        val w = size.width
        val h = size.height
        moveTo(0f, h)
        cubicTo(w * 0.1f, h, w * 0.15f, h * 0.7f, w * 0.2f, h * 0.35f)
        cubicTo(w * 0.3f, 0f, w * 0.7f, 0f, w * 0.8f, h * 0.35f)
        cubicTo(w * 0.85f, h * 0.7f, w * 0.9f, h, w, h)
        close()
    }

    Box(
        modifier = modifier
            .pointerInput(Unit) {
                detectTapGestures(onPress = {
                    isHolding = true
                    try { awaitRelease() } finally { isHolding = false }
                })
            },
        contentAlignment = Alignment.BottomCenter
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .clip(bellShape)
                .background(LuklanColors.Secondary),
            contentAlignment = Alignment.BottomCenter
        ) {
            Box(modifier = Modifier.fillMaxWidth().fillMaxHeight(progress).background(LuklanColors.Primary))
            Text("ฉุกเฉิน", color = Color.White, style = LuklanTypography.h3, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 32.dp))
        }
    }
}

@Composable
fun EmergencyScreen(userProfile: User?, onBack: () -> Unit) {
    val alertRepository = remember { getAlertRepository() }
    val authRepository = remember { getAuthRepository() }
    val scope = rememberCoroutineScope()
    var isSent by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val userId = authRepository.getCurrentUserId()
        if (userId != null) {
            println("🆘 SOS Triggered. Fetching fresh profile for $userId")
            authRepository.getUserProfile(userId).onSuccess { freshUser ->
                println("🆘 Fresh profile groupIds: ${freshUser.groupIds}")
                val alert = com.commu.luklan.data.Alert(
                    senderId = freshUser.id,
                    senderName = freshUser.name,
                    type = "SOS",
                    message = "${freshUser.name} ต้องการความช่วยเหลือด่วน!",
                    timestamp = getCurrentTimeMillis(),
                    groupIds = freshUser.groupIds
                )
                alertRepository.sendAlert(alert).onSuccess { 
                    println("✅ SOS Alert saved to DB")
                    isSent = true 
                }.onFailure { println("❌ Failed to save SOS: ${it.message}") }
            }.onFailure { println("❌ Failed to fetch fresh profile: ${it.message}") }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(LuklanColors.Primary).padding(LuklanSpacing.lg), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(24.dp)) {
            Box(Modifier.size(120.dp).clip(CircleShape).background(Color.White), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.LocalHospital, null, tint = LuklanColors.Error, modifier = Modifier.size(80.dp))
            }
            Text(
                if (isSent) "ส่งสัญญาณฉุกเฉินแล้ว!" else "กำลังส่งสัญญาณฉุกเฉิน...", 
                style = LuklanTypography.h1, fontWeight = FontWeight.Bold, color = Color.White, textAlign = TextAlign.Center
            )
            Text("ผู้ดูแลของคุณได้รับแจ้งเตือนแล้ว", style = LuklanTypography.bodyLarge, color = Color.White.copy(alpha = 0.8f))
            Button(onClick = onBack, colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = LuklanColors.Primary), shape = RoundedCornerShape(28.dp), modifier = Modifier.fillMaxWidth().height(56.dp)) {
                Text("ย้อนกลับ", style = LuklanTypography.h3)
            }
        }
    }
}

@Composable
fun PillIcon(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(Res.drawable.capsule2),
        contentDescription = null,
        modifier = modifier
    )
}

@Composable
fun MenuScreen(
    userRole: String?,
    onNavigateToProfile: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToMedicineGroups: () -> Unit,
    onNavigateToInviteCaretaker: () -> Unit,
    onNavigateToCaretakerDashboard: () -> Unit,
    onNavigateToNotificationCenter: (String) -> Unit
) {
    var searchText by remember { mutableStateOf("") }
    Column(
        modifier = Modifier.fillMaxSize().background(LuklanColors.Background),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(48.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = LuklanSpacing.lg),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "เมนู", 
                style = LuklanTypography.h1, 
                fontWeight = FontWeight.Bold, 
                color = LuklanColors.Primary,
                fontSize = 32.sp
            )
        }
        
        Spacer(Modifier.height(24.dp))
        
        OutlinedTextField(
            value = searchText, 
            onValueChange = { searchText = it }, 
            modifier = Modifier.fillMaxWidth().padding(horizontal = LuklanSpacing.lg), 
            placeholder = { Text("ค้นหา") }, 
            leadingIcon = { Icon(Icons.Default.Search, null, tint = LuklanColors.Primary) }, 
            shape = RoundedCornerShape(50.dp), 
            colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White), 
            singleLine = true
        )
        
        Spacer(Modifier.height(LuklanSpacing.lg))
        
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = LuklanSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            MenuCardWide(
                icon = Icons.Default.Medication,
                label = "กลุ่มยา", 
                onClick = onNavigateToMedicineGroups, 
                color = LuklanColors.Primary
            )
            
            MenuCardWide(
                icon = Icons.Default.History, 
                label = "ประวัติการกินยา", 
                onClick = onNavigateToHistory, 
                color = LuklanColors.Primary
            )
            
            MenuCardWide(
                icon = Icons.Default.People, 
                label = "กลุ่มผู้ดูแล", 
                onClick = onNavigateToCaretakerDashboard, 
                color = LuklanColors.Primary
            )
        }
        
        Spacer(Modifier.weight(1f))
        
        ListItem(
            headlineContent = { Text("ข้อมูลส่วนตัว", style = LuklanTypography.bodyLarge) }, 
            leadingContent = { Icon(Icons.Default.Person, null, tint = LuklanColors.Primary) }, 
            trailingContent = { Icon(Icons.Default.ChevronRight, null) }, 
            modifier = Modifier.clickable { onNavigateToProfile() }
        )
    }
}

@Composable
fun MenuCardWide(
    icon: Any, 
    label: String, 
    onClick: () -> Unit, 
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth().height(140.dp), 
        colors = CardDefaults.cardColors(containerColor = color), 
        shape = RoundedCornerShape(24.dp), 
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Box(
                modifier = Modifier.size(80.dp),
                contentAlignment = Alignment.Center
            ) {
                if (label == "กลุ่มยา") {
                    PillIcon(modifier = Modifier.size(65.dp))
                } else if (icon is org.jetbrains.compose.resources.DrawableResource) {
                    Image(
                        painter = painterResource(icon),
                        contentDescription = null,
                        modifier = Modifier.size(75.dp)
                    )
                } else if (icon is androidx.compose.ui.graphics.vector.ImageVector) {
                    Icon(
                        icon, 
                        null, 
                        tint = Color.White, 
                        modifier = Modifier.size(70.dp)
                    )
                }
            }
            
            Spacer(Modifier.width(24.dp))
            
            Text(
                label, 
                color = Color.White, 
                style = LuklanTypography.h3,
                fontWeight = FontWeight.Bold, 
                textAlign = TextAlign.Start,
                modifier = Modifier.weight(1f),
                maxLines = 2,
                softWrap = true
            )
        }
    }
}
