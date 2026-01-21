package com.commu.luklan.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.commu.luklan.data.AuthRepository
import com.commu.luklan.ui.theme.LuklanTheme
import com.commu.luklan.ui.theme.LuklanTypography
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(onNavigateBack: () -> Unit, onLogoutSuccess: () -> Unit) {
        val authRepository = remember { AuthRepository() }
        val scope = rememberCoroutineScope()

        Scaffold(
                topBar = {
                        TopAppBar(
                                title = { Text("ข้อมูลส่วนตัว", style = LuklanTypography.h3) },
                                navigationIcon = {
                                        IconButton(onClick = onNavigateBack) {
                                                Icon(
                                                        Icons.AutoMirrored.Filled.ArrowBack,
                                                        contentDescription = "Back"
                                                )
                                        }
                                },
                                colors =
                                        TopAppBarDefaults.topAppBarColors(
                                                containerColor = LuklanTheme.colors.Background
                                        )
                        )
                },
                containerColor = LuklanTheme.colors.Background
        ) { paddingValues ->
                Column(
                        modifier =
                                Modifier.fillMaxSize()
                                        .padding(paddingValues)
                                        .padding(LuklanTheme.spacing.md),
                        horizontalAlignment = Alignment.CenterHorizontally
                ) {
                        Spacer(modifier = Modifier.height(LuklanTheme.spacing.xl))

                        // Profile Image Placeholder
                        Box(
                                modifier =
                                        Modifier.size(120.dp)
                                                .clip(CircleShape)
                                                .background(Color.LightGray),
                                contentAlignment = Alignment.Center
                        ) { Text("👤", style = LuklanTypography.h1) }

                        Spacer(modifier = Modifier.height(LuklanTheme.spacing.md))

                        Text(
                                text = "คุณยาย", // Placeholder name
                                style = LuklanTypography.h2,
                                color = LuklanTheme.colors.TextPrimary
                        )

                        Spacer(modifier = Modifier.height(LuklanTheme.spacing.xxl))

                        // Logout Button
                        Button(
                                onClick = {
                                        scope.launch {
                                                authRepository.signOut()
                                                onLogoutSuccess()
                                        }
                                },
                                colors =
                                        ButtonDefaults.buttonColors(
                                                containerColor = LuklanTheme.colors.Error,
                                                contentColor = Color.White
                                        ),
                                modifier = Modifier.fillMaxWidth()
                        ) { Text("ออกจากระบบ", style = LuklanTypography.buttonLarge) }
                }
        }
}
