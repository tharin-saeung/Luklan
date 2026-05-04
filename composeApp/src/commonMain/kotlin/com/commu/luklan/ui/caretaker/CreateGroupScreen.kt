package com.commu.luklan.ui.caretaker

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.commu.luklan.data.getAuthRepository
import com.commu.luklan.data.getGroupRepository
import com.commu.luklan.data.getStorageRepository
import com.commu.luklan.ui.theme.LuklanColors
import com.commu.luklan.ui.theme.LuklanSpacing
import com.commu.luklan.ui.theme.LuklanTheme.LuklanTypography
import com.commu.luklan.ui.components.ImageSelector
import coil3.compose.AsyncImage
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateGroupScreen(onBack: () -> Unit, onSuccess: () -> Unit) {
    var groupName by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var selectedImageBytes by remember { mutableStateOf<ByteArray?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    val scope = rememberCoroutineScope()
    val groupRepository = remember { getGroupRepository() }
    val authRepository = remember { getAuthRepository() }
    val storageRepository = remember { getStorageRepository() }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("สร้างกลุ่มใหม่", style = LuklanTypography.h3, fontWeight = FontWeight.Bold, color = LuklanColors.Primary) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = LuklanColors.Primary)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = LuklanColors.Background)
            )
        },
        containerColor = LuklanColors.Background
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(LuklanSpacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Group Image Picker
            ImageSelector(
                image = selectedImageBytes,
                onImageSelected = { bytes ->
                    if (bytes != null) {
                        selectedImageBytes = bytes
                    }
                },
                size = 120.dp,
                placeholder = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Groups, null, tint = LuklanColors.Primary, modifier = Modifier.size(48.dp))
                        Text("เพิ่มรูปกลุ่ม", style = LuklanTypography.bodySmall, color = LuklanColors.Primary)
                    }
                }
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "ระบุชื่อกลุ่มสำหรับดูแลผู้ป่วย",
                    style = LuklanTypography.h3,
                    color = LuklanColors.TextPrimary
                )
                Text(
                    "สมาชิกในกลุ่มจะสามารถช่วยกันดูแลผู้ป่วยได้",
                    style = LuklanTypography.bodyMedium,
                    color = LuklanColors.TextSecondary
                )
            }

            OutlinedTextField(
                value = groupName,
                onValueChange = { groupName = it },
                label = { Text("ชื่อกลุ่ม") },
                placeholder = { Text("เช่น กลุ่มดูแลคุณพ่อ") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = LuklanColors.Primary,
                    unfocusedBorderColor = Color.LightGray
                )
            )

            if (errorMessage != null) {
                Surface(
                    color = LuklanColors.Error.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(errorMessage!!, color = LuklanColors.Error, style = LuklanTypography.bodySmall, modifier = Modifier.padding(8.dp))
                }
            }

            Spacer(modifier = Modifier.weight(1f))

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
                                groupRepository.createGroup(groupName, user).onSuccess { group ->
                                    if (selectedImageBytes != null) {
                                        storageRepository.uploadImage("groups/${group.id}.jpg", selectedImageBytes!!).onSuccess { url ->
                                            groupRepository.updateGroupPhoto(group.id, url).onSuccess {
                                                onSuccess()
                                            }
                                        }.onFailure {
                                            // Even if photo upload fails, group is created
                                            onSuccess()
                                        }
                                    } else {
                                        onSuccess()
                                    }
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
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LuklanColors.Primary),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
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
