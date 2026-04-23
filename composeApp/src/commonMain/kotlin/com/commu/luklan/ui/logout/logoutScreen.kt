package com.commu.luklan.ui.logout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.commu.luklan.data.User
import com.commu.luklan.data.getAuthRepository
import com.commu.luklan.ui.theme.LuklanTheme
import com.commu.luklan.ui.theme.LuklanTypography
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun logoutScreen(onNavigateBack: () -> Unit, onLogoutSuccess: () -> Unit) {
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
        topBar = {
            TopAppBar(
                title = { Text("Profile", style = LuklanTypography.h3) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = LuklanTheme.colors.Background
                )
            )
        },
        containerColor = LuklanTheme.colors.Background
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize()
                .padding(paddingValues)
                .padding(LuklanTheme.spacing.md),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(LuklanTheme.spacing.xl))

            // Profile Image Placeholder
            Box(
                modifier = Modifier.size(120.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = LuklanTheme.colors.Primary,
                    modifier = Modifier.size(80.dp)
                )
            }

            Spacer(modifier = Modifier.height(LuklanTheme.spacing.md))

            if (isLoading) {
                CircularProgressIndicator(color = LuklanTheme.colors.Primary)
            } else if (userProfile != null) {
                Text(
                    text = userProfile!!.name,
                    style = LuklanTypography.h2,
                    color = LuklanTheme.colors.TextPrimary
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
                    color = LuklanTheme.colors.TextSecondary
                )

                Text(
                    text = userProfile!!.email,
                    style = LuklanTypography.bodySmall,
                    color = LuklanTheme.colors.TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(LuklanTheme.spacing.xxl))

            // Logout Button
            Button(
                onClick = {
                    scope.launch {
                        com.commu.luklan.data.getNotificationScheduler().cancelAll()
                        authRepository.signOut()
                        onLogoutSuccess()
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = LuklanTheme.colors.Error,
                    contentColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(28.dp)
            ) {
                Text("ออกจากระบบ", style = LuklanTypography.h3)
            }
        }
    }
}
