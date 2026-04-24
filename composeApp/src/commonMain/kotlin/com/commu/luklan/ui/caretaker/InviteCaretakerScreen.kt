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
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import kotlinx.coroutines.launch
import qrgenerator.qrkitpainter.rememberQrKitPainter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InviteCaretakerScreen(
    onBack: () -> Unit
) {
    val authRepository = remember { getAuthRepository() }
    val groupRepository = remember { getGroupRepository() }
    val scope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    
    var group by remember { mutableStateOf<CareGroup?>(null) }
    var userName by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }

    val qrPainter = group?.inviteCode?.let { 
        rememberQrKitPainter(data = it)
    }

    fun loadInfo() {
        val userId = authRepository.getCurrentUserId()
        if (userId != null) {
            scope.launch {
                authRepository.getUserProfile(userId).onSuccess { user ->
                    userName = user.name
                    groupRepository.getGroupsForUser(userId).onSuccess { groups ->
                        group = groups.firstOrNull()
                        isLoading = false
                    }.onFailure { isLoading = false }
                }.onFailure { isLoading = false }
            }
        }
    }

    LaunchedEffect(Unit) { loadInfo() }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("เชิญผู้ดูแล", style = LuklanTypography.h1, fontWeight = FontWeight.Bold) },
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
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(LuklanSpacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = LuklanColors.Primary)
            } else {
                Spacer(Modifier.weight(1f))
                
                // Invite Card
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(32.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // User Avatar Proxy
                        Box(
                            modifier = Modifier.size(80.dp).clip(CircleShape).background(LuklanColors.Primary.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, null, tint = LuklanColors.Primary, modifier = Modifier.size(48.dp))
                        }
                        
                        Spacer(Modifier.height(16.dp))
                        
                        Text(text = userName, style = LuklanTypography.h2, fontWeight = FontWeight.Bold)
                        Text(text = "รหัสเชิญกลุ่ม", style = LuklanTypography.bodySmall, color = LuklanColors.TextSecondary)
                        
                        Spacer(Modifier.height(8.dp))
                        
                        Text(
                            text = group?.inviteCode ?: "-----", 
                            style = LuklanTypography.h1, 
                            color = LuklanColors.Primary,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black
                        )
                        
                        Spacer(Modifier.height(24.dp))
                        
                        // QR Code Generator
                        Box(
                            modifier = Modifier.size(180.dp).background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            if (qrPainter != null) {
                                androidx.compose.foundation.Image(
                                    painter = qrPainter,
                                    contentDescription = "Invite QR Code",
                                    modifier = Modifier.size(160.dp)
                                )
                            } else {
                                Icon(Icons.Default.QrCode2, null, modifier = Modifier.size(160.dp), tint = Color.LightGray)
                            }
                        }
                    }
                }
                
                Spacer(Modifier.height(32.dp))
                
                // Bottom Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    InviteButton(
                        icon = Icons.Default.ContentCopy,
                        label = "คัดลอกรหัส",
                        modifier = Modifier.fillMaxWidth(0.6f),
                        onClick = {
                            group?.inviteCode?.let { code ->
                                clipboardManager.setText(AnnotatedString(code))
                                scope.launch {
                                    snackbarHostState.showSnackbar("คัดลอกรหัสแล้ว")
                                }
                            }
                        }
                    )
                }
                
                Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun InviteButton(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, modifier: Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier.height(60.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = borderStroke(1.dp, LuklanColors.Primary.copy(alpha = 0.2f)),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(icon, null, tint = LuklanColors.Primary, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(8.dp))
            Text(text = label, style = LuklanTypography.bodyLarge, fontWeight = FontWeight.Bold, color = LuklanColors.Primary)
        }
    }
}

private fun borderStroke(width: androidx.compose.ui.unit.Dp, color: Color) = androidx.compose.foundation.BorderStroke(width, color)
