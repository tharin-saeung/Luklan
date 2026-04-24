package com.commu.luklan.ui.caretaker

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.commu.luklan.data.getAuthRepository
import com.commu.luklan.data.getGroupRepository
import com.commu.luklan.ui.theme.LuklanColors
import com.commu.luklan.ui.theme.LuklanSpacing
import com.commu.luklan.ui.theme.LuklanTypography
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateGroupScreen(onBack: () -> Unit, onSuccess: () -> Unit) {
    var groupName by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val groupRepository = remember { getGroupRepository() }
    val authRepository = remember { getAuthRepository() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("สร้างกลุ่มใหม่", style = LuklanTypography.h2, fontWeight = FontWeight.Bold) },
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
            modifier = Modifier.fillMaxSize().padding(padding).padding(LuklanSpacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                "กรุณาระบุชื่อกลุ่มสำหรับดูแลผู้ป่วย",
                style = LuklanTypography.bodyLarge,
                color = LuklanColors.TextSecondary
            )

            OutlinedTextField(
                value = groupName,
                onValueChange = { groupName = it },
                label = { Text("ชื่อกลุ่ม") },
                placeholder = { Text("เช่น กลุ่มดูแลคุณพ่อ") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            if (errorMessage != null) {
                Text(errorMessage!!, color = LuklanColors.Error, style = LuklanTypography.bodySmall)
            }

            Button(
                onClick = {
                    if (groupName.isBlank()) {
                        errorMessage = "กรุณาระบุชื่อกลุ่ม"
                        return@Button
                    }
                    isLoading = true
                    scope.launch {
                        val userId = authRepository.getCurrentUserId()
                        if (userId != null) {
                            authRepository.getUserProfile(userId).onSuccess { user ->
                                groupRepository.createGroup(groupName, user).onSuccess {
                                    onSuccess()
                                }.onFailure {
                                    errorMessage = it.message
                                    isLoading = false
                                }
                            }.onFailure {
                                errorMessage = "ไม่พบข้อมูลผู้ใช้"
                                isLoading = false
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LuklanColors.Primary)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("สร้างกลุ่ม", style = LuklanTypography.buttonLarge)
                }
            }
        }
    }
}
