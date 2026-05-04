package com.commu.luklan.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.commu.luklan.data.AuthRepository
import com.commu.luklan.data.User
import com.commu.luklan.data.getAuthRepository
import com.commu.luklan.data.getStorageRepository
import com.commu.luklan.ui.theme.LuklanColors
import com.commu.luklan.ui.theme.LuklanTheme
import com.commu.luklan.ui.theme.LuklanTheme.LuklanTypography
import com.commu.luklan.ui.components.ImageSelector
import coil3.compose.AsyncImage
import kotlinx.coroutines.launch
import com.commu.luklan.data.AppCache
import com.commu.luklan.data.getNotificationScheduler

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit,
    onNavigateToGroups: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToContact: () -> Unit,
    onLogoutSuccess: () -> Unit
) {
    val authRepository = remember { getAuthRepository() }
    val storageRepository = remember { getStorageRepository() }
    val notificationScheduler = remember { getNotificationScheduler() }
    val scope = rememberCoroutineScope()

    val userId = remember { authRepository.getCurrentUserId() ?: "" }
    var userProfile by remember { mutableStateOf<User?>(AppCache.userProfileCache[userId]) }
    var isLoading by remember { mutableStateOf(userProfile == null) }
    var isUploading by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (userId.isNotEmpty()) {
            authRepository.getUserProfile(userId).onSuccess {
                userProfile = it
                AppCache.userProfileCache[userId] = it
                isLoading = false
            }.onFailure {
                isLoading = false
            }
        } else {
            isLoading = false
        }
    }

    Scaffold(
        containerColor = Color.White
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                // HEADER - Blue section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF3F6E8C))
                        .padding(top = padding.calculateTopPadding()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(24.dp))
                    // Top bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Profile Icon
                    ImageSelector(
                        image = userProfile?.photoUrl ?: "",
                        isUploading = isUploading,
                        onImageSelected = { bytes ->
                            if (bytes != null) {
                                userProfile?.id?.let { uid ->
                                    scope.launch {
                                        isUploading = true
                                        storageRepository.uploadImage("profiles/$uid.jpg", bytes).onSuccess { url ->
                                            authRepository.updateUserPhoto(uid, url).onSuccess {
                                                userProfile = userProfile?.copy(photoUrl = url)
                                            }
                                        }
                                        isUploading = false
                                    }
                                }
                            }
                        },
                        size = 96.dp,
                        placeholder = {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(56.dp)
                            )
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White)
                    } else {
                        Text(
                            text = userProfile?.name ?: "",
                            color = Color.White,
                            style = LuklanTypography.h2,
                            fontWeight = FontWeight.Bold
                        )

                        val roleText = when (userProfile?.role) {
                            "patient" -> "ผู้ป่วย"
                            "caretaker" -> "ผู้ดูแล"
                            else -> "ผู้ใช้งานทั่วไป"
                        }
                        Text(
                            text = roleText,
                            style = LuklanTypography.bodyLarge,
                            color = Color.White.copy(alpha = 0.8f)
                        )

                        Text(
                            text = userProfile?.email ?: "",
                            style = LuklanTypography.bodySmall,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }

                // MENU Area - Use Box to overlap header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .offset(y = (-24).dp) // Overlap the blue header to show rounding
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxSize(),
                        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                        color = Color.White
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 24.dp, vertical = 24.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            MenuItem(title = "กลุ่มผู้ดูแล", icon = Icons.Default.Groups, onClick = onNavigateToGroups)
                            Divider(color = Color.LightGray.copy(alpha = 0.5f))
                            MenuItem(title = "ประวัติการใช้ยา", icon = Icons.Default.History, onClick = onNavigateToHistory)
                            Divider(color = Color.LightGray.copy(alpha = 0.5f))
                            MenuItem(title = "ติดต่อทีมงาน", icon = Icons.Default.ContactSupport, onClick = onNavigateToContact)
                            Divider(color = Color.LightGray.copy(alpha = 0.5f))
                            MenuItem(
                                title = "ออกจากระบบ",
                                icon = Icons.Default.Logout,
                                isLogout = true,
                                onClick = { showLogoutDialog = true }
                            )
                            Divider(color = Color.LightGray.copy(alpha = 0.5f))
                            TextButton(
                                onClick = { showDeleteAccountDialog = true },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.textButtonColors(contentColor = LuklanColors.Error)
                            ) {
                                Text(
                                    "ลบบัญชีผู้ใช้",
                                    style = LuklanTypography.bodySmall,
                                    textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            containerColor = Color.White,
            shape = RoundedCornerShape(24.dp),
            title = { Text("ออกจากระบบ?", style = LuklanTypography.h3, fontWeight = FontWeight.Bold, color = LuklanColors.Primary) },
            text = { Text("คุณต้องการออกจากระบบใช่หรือไม่?", style = LuklanTypography.bodyLarge, color = LuklanColors.TextPrimary) },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        scope.launch {
                            notificationScheduler.cancelAll()
                            authRepository.signOut()
                            AppCache.clear()
                            onLogoutSuccess()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LuklanColors.Error),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("ออกจากระบบ", color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) { Text("ยกเลิก", color = LuklanColors.TextSecondary) }
            }
        )
    }

    if (showDeleteAccountDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAccountDialog = false },
            containerColor = Color.White,
            shape = RoundedCornerShape(24.dp),
            title = { Text("ลบบัญชีผู้ใช้?", style = LuklanTypography.h3, fontWeight = FontWeight.Bold, color = LuklanColors.Error) },
            text = { Text("การดำเนินการนี้ไม่สามารถย้อนกลับได้ ข้อมูลทั้งหมดของคุณจะถูกลบถาวร คุณต้องการลบบัญชีใช่หรือไม่?", style = LuklanTypography.bodyLarge, color = LuklanColors.TextPrimary) },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteAccountDialog = false
                        scope.launch {
                            notificationScheduler.cancelAll()
                            authRepository.deleteAccount().onSuccess {
                                AppCache.clear()
                                onLogoutSuccess()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LuklanColors.Error),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("ยืนยันการลบ", color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAccountDialog = false }) { Text("ยกเลิก", color = LuklanColors.TextSecondary) }
            }
        )
    }
}

@Composable
fun MenuItem(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isLogout: Boolean = false,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isLogout) LuklanColors.Error else Color.Gray,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            color = if (isLogout) LuklanColors.Error else Color.Black,
            modifier = Modifier.weight(1f),
            style = LuklanTypography.bodyLarge
        )
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = Color.Gray,
            modifier = Modifier.size(20.dp)
        )
    }
}
