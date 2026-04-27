package com.commu.luklan.ui.caretaker

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.commu.luklan.data.AuthRepository
import com.commu.luklan.data.CareGroup
import com.commu.luklan.data.getAuthRepository
import com.commu.luklan.data.getGroupRepository
import com.commu.luklan.ui.theme.*
import coil3.compose.AsyncImage
import kotlinx.coroutines.launch
import qrgenerator.qrkitpainter.rememberQrKitPainter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InviteCaretakerScreen(
    groupId: String? = null,
    onBack: () -> Unit
) {
    val authRepository = remember { getAuthRepository() }
    val groupRepository = remember { getGroupRepository() }
    val scope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    
    var group by remember { mutableStateOf<CareGroup?>(null) }
    var userProfile by remember { mutableStateOf<com.commu.luklan.data.User?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    val qrPainter = group?.inviteCode?.let { 
        rememberQrKitPainter(data = it)
    }

    fun loadInfo() {
        val userId = authRepository.getCurrentUserId()
        if (userId != null) {
            scope.launch {
                authRepository.getUserProfile(userId).onSuccess { user ->
                    userProfile = user
                    if (groupId != null) {
                        groupRepository.getGroupById(groupId).onSuccess { g ->
                            group = g
                            isLoading = false
                        }.onFailure { isLoading = false }
                    } else {
                        groupRepository.getGroupsForUser(userId).onSuccess { groups ->
                            group = groups.firstOrNull()
                            isLoading = false
                        }.onFailure { isLoading = false }
                    }
                }.onFailure { isLoading = false }
            }
        }
    }

    LaunchedEffect(Unit) { loadInfo() }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("เชิญผู้ดูแล", style = LuklanTypography.h1, color = LuklanColors.Primary, fontWeight = FontWeight.Bold) },
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
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = LuklanColors.Primary)
            } else {
                // Invite Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 40.dp),
                    shape = RoundedCornerShape(32.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // User Profile Pic
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(LuklanColors.Primary.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!userProfile?.photoUrl.isNullOrEmpty()) {
                                AsyncImage(
                                    model = userProfile?.photoUrl,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(Icons.Default.Person, null, tint = LuklanColors.Primary, modifier = Modifier.size(48.dp))
                            }
                        }
                        
                        Spacer(Modifier.height(16.dp))
                        
                        Text(
                            text = userProfile?.name ?: "", 
                            style = LuklanTypography.h2, 
                            fontWeight = FontWeight.Bold,
                            color = LuklanColors.TextPrimary
                        )
                        Text(
                            text = "รหัสเชิญกลุ่ม", 
                            style = LuklanTypography.bodySmall, 
                            color = LuklanColors.TextSecondary
                        )
                        
                        Spacer(Modifier.height(12.dp))
                        
                        Text(
                            text = group?.inviteCode ?: "-----", 
                            style = LuklanTypography.h1, 
                            color = LuklanColors.Primary,
                            fontSize = 40.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 4.sp
                        )
                        
                        Spacer(Modifier.height(32.dp))
                        
                        // QR Code
                        Box(
                            modifier = Modifier
                                .size(200.dp)
                                .background(Color.White)
                                .border(1.dp, Color.LightGray.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (qrPainter != null) {
                                androidx.compose.foundation.Image(
                                    painter = qrPainter,
                                    contentDescription = "Invite QR Code",
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Icon(Icons.Default.QrCode2, null, modifier = Modifier.size(160.dp), tint = Color.LightGray)
                            }
                        }
                    }
                }
                
                Spacer(Modifier.height(40.dp))
                
                // Copy Button
                Button(
                    onClick = {
                        group?.inviteCode?.let { code ->
                            clipboardManager.setText(AnnotatedString(code))
                            scope.launch {
                                snackbarHostState.showSnackbar("คัดลอกรหัสแล้ว")
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                    border = borderStroke(1.dp, LuklanColors.Primary.copy(alpha = 0.2f))
                ) {
                    Icon(Icons.Default.ContentCopy, null, tint = LuklanColors.Primary, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = "คัดลอกรหัส", 
                        style = LuklanTypography.bodyLarge, 
                        fontWeight = FontWeight.Bold, 
                        color = LuklanColors.Primary
                    )
                }
            }
        }
    }
}

private fun borderStroke(width: androidx.compose.ui.unit.Dp, color: Color) = androidx.compose.foundation.BorderStroke(width, color)
