package com.commu.luklan.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.commu.luklan.data.AuthRepository
import com.commu.luklan.data.User
import com.commu.luklan.data.getAuthRepository
import com.commu.luklan.ui.theme.LuklanTheme
import com.commu.luklan.ui.theme.LuklanTypography
import kotlinx.coroutines.launch
import qrgenerator.qrkitpainter.text

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(onNavigateBack: () -> Unit, onGoToLogoutScreen: () -> Unit) {
    val authRepository = remember { getAuthRepository() }
    val scope = rememberCoroutineScope()

    var userProfile by remember { mutableStateOf<User?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val userId = authRepository.getCurrentUserId()
        if (userId != null) {
            authRepository.getUserProfile(userId).onSuccess {
                userProfile = it
                isLoading = false
            }.onFailure {
                isLoading = false
            }
        } else {
            isLoading = false
        }
    }

    Scaffold(
        containerColor = Color(0xFF3F6E8C)//0xFFF5F5F5
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            // 🔷 HEADER
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340.dp)
//                    .padding(bottom = 20.dp)
                    .background(Color(0xFF3F6E8C))
            ) {

                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Spacer(modifier = Modifier.height(24.dp))

                    // Top bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                        }

                        IconButton(onClick = { }) {
                            Icon(Icons.Default.MoreVert, null, tint = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Profile
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .clip(CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(56.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White)
                    } else {
                        Text(
                            text = userProfile?.name ?: "",
                            color = Color.White,
                            style = LuklanTypography.h1,
                            fontSize = 18.sp
                        )

                        fun getRoleText(role: String?): String {
                            return when (role) {
                                "patient" -> "ผู้ป่วย"
                                "caretaker" -> "ผู้ดูแล"
                                else -> "ผู้ใช้งานทั่วไป"
                            }
                        }
                        Text(
                            text = getRoleText(userProfile?.role),
                            style = LuklanTypography.bodyLarge,
                            color = LuklanTheme.colors.OnPrimary
                        )

                        Text(
                            text = userProfile?.email?: "",
                            style = LuklanTypography.bodySmall,
                            color = LuklanTheme.colors.OnPrimary
                        )
                    }
                }
            }

            // ⚪ CARD MENU
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .offset(y = 130.dp),
                shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {

                Column(
                    modifier = Modifier.padding(16.dp)
                ) {

                    MenuItem(title = "กลุ่ม")
                    Divider()

                    MenuItem(title = "ประวัติการกินยา")
                    Divider()

                    MenuItem(title = "ติดต่อสอบถาม")
                    Divider()

                    MenuItem(
                        title = "Logout",
                        isLogout = true,
                        onClick = {

                                onGoToLogoutScreen()

                        }
                    )
                }
            }
        }
    }
}

@Composable
fun MenuItem(
    title: String,
    isLogout: Boolean = false,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Icon(
            imageVector = if (isLogout) Icons.Default.Close else Icons.Default.Person,
            contentDescription = null,
            tint = if (isLogout) Color.Red else Color.Gray
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = title,
            color = if (isLogout) Color.Red else Color.Black,
            modifier = Modifier.weight(1f),
            fontSize = 16.sp
        )

        Icon(
            imageVector = Icons.Default.ArrowForward,
            contentDescription = null,
            tint = Color.Gray
        )
    }
}
