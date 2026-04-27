package com.commu.luklan.ui.caretaker

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.commu.luklan.data.getAuthRepository
import com.commu.luklan.data.getGroupRepository
import com.commu.luklan.ui.components.OtpTextField
import com.commu.luklan.ui.theme.*
import kotlinx.coroutines.launch
import qrscanner.QrScanner
import qrscanner.CameraLens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JoinCaretakerScreen(
    onBack: () -> Unit,
    onSuccess: (groupId: String) -> Unit
) {
    val authRepository = remember { getAuthRepository() }
    val groupRepository = remember { getGroupRepository() }
    val scope = rememberCoroutineScope()
    
    var inviteCode by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var isScanning by remember { mutableStateOf(false) }
    var cameraLens by remember { mutableStateOf(CameraLens.Back) }
    var openImagePicker by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    if (isScanning) {
        Box(modifier = Modifier.fillMaxSize()) {
            QrScanner(
                modifier = Modifier.fillMaxSize(),
                flashlightOn = false,
                cameraLens = cameraLens,
                openImagePicker = openImagePicker,
                onCompletion = { result: String ->
                    if (result.length == 5) {
                        inviteCode = result.uppercase()
                    }
                    isScanning = false
                },
                onFailure = { error: String ->
                    errorMessage = error
                    isScanning = false
                },
                imagePickerHandler = { openImagePicker = it }
            )
            
            // Back button on top of scanner
            IconButton(
                onClick = { isScanning = false },
                modifier = Modifier.padding(16.dp).statusBarsPadding()
                    .align(Alignment.TopStart)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(Icons.Default.ArrowBack, null, tint = Color.White)
            }
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("เพิ่มกลุ่ม", style = LuklanTypography.h1, color = LuklanColors.Primary, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = LuklanColors.Primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = LuklanColors.Background)
            )
        },
        containerColor = LuklanColors.Background
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .clip(RoundedCornerShape(32.dp))
                    .background(Color.White)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.Cancel, null, tint = LuklanColors.TextSecondary)
                    }
                }
                
                Icon(
                    Icons.Default.QrCodeScanner, 
                    null, 
                    tint = LuklanColors.Primary, 
                    modifier = Modifier.size(64.dp)
                )
                
                Spacer(Modifier.height(16.dp))
                
                Text(
                    "กรอกรหัสเชิญกลุ่ม",
                    style = LuklanTypography.h2, 
                    fontWeight = FontWeight.Bold,
                    color = LuklanColors.TextPrimary
                )
                
                Spacer(Modifier.height(24.dp))
                
                OtpTextField(
                    otpText = inviteCode,
                    onOtpTextChange = { 
                        inviteCode = it
                        errorMessage = null 
                    }
                )
                
                if (errorMessage != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(errorMessage!!, color = LuklanColors.Error, style = LuklanTypography.bodySmall)
                }
                
                Spacer(Modifier.height(32.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Scan button
                    OutlinedButton(
                        onClick = { isScanning = true },
                        modifier = Modifier.weight(1f).height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = LuklanColors.Primary)
                    ) {
                        Icon(Icons.Default.QrCodeScanner, null)
                        Spacer(Modifier.width(8.dp))
                        Text("แสกน", fontWeight = FontWeight.Bold)
                    }
                    
                    // Confirm button
                    Button(
                        onClick = {
                            if (inviteCode.length == 5) {
                                val userId = authRepository.getCurrentUserId() ?: return@Button
                                isLoading = true
                                scope.launch {
                                    groupRepository.joinGroup(userId, inviteCode)
                                        .onSuccess { joinedGroup ->
                                            isLoading = false
                                            onSuccess(joinedGroup.id)
                                        }
                                        .onFailure { 
                                            isLoading = false
                                            errorMessage = it.message ?: "ไม่สามารถเข้าร่วมกลุ่มได้"
                                        }
                                }
                            }
                        },
                        modifier = Modifier.weight(1f).height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = LuklanColors.Primary),
                        enabled = inviteCode.length == 5 && !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Icon(Icons.Default.Check, null)
                            Spacer(Modifier.width(8.dp))
                            Text("ยืนยัน", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
