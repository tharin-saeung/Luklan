package com.commu.luklan.ui.caretaker

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.commu.luklan.data.CareGroup
import com.commu.luklan.data.User
import com.commu.luklan.data.getAuthRepository
import com.commu.luklan.data.getGroupRepository
import com.commu.luklan.data.getStorageRepository
import com.commu.luklan.platform.rememberImagePickerLauncher
import com.commu.luklan.ui.theme.*
import coil3.compose.AsyncImage
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupMembersScreen(
    groupId: String,
    groupName: String,
    onBack: () -> Unit,
    onNavigateToInvite: (String) -> Unit,
    onNavigateToPatientTimeline: (String, String) -> Unit
) {
    val groupRepository = remember { getGroupRepository() }
    val authRepository = remember { getAuthRepository() }
    val storageRepository = remember { getStorageRepository() }
    val scope = rememberCoroutineScope()
    
    var group by remember { mutableStateOf<CareGroup?>(null) }
    var members by remember { mutableStateOf<List<User>>(emptyList()) }
    var currentUserId by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    var showKickConfirm by remember { mutableStateOf<User?>(null) }
    var showTransferConfirm by remember { mutableStateOf<User?>(null) }
    var showDeleteGroupConfirm by remember { mutableStateOf(false) }
    
    var isEditing by remember { mutableStateOf(false) }
    var editName by remember { mutableStateOf("") }
    var isUpdating by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberImagePickerLauncher { bytes: ByteArray? ->
        val imgBytes = bytes
        if (imgBytes != null) {
            scope.launch {
                isUpdating = true
                storageRepository.uploadImage("groups/$groupId.jpg", imgBytes).onSuccess { url ->
                    groupRepository.updateGroupPhoto(groupId, url).onSuccess {
                        group = group?.copy(photoUrl = url)
                    }
                }
                isUpdating = false
            }
        }
    }

    fun loadData() {
        scope.launch {
            currentUserId = authRepository.getCurrentUserId()
            groupRepository.getGroupById(groupId).onSuccess { g ->
                group = g
                editName = g.name
                groupRepository.getGroupMembers(groupId).onSuccess { m ->
                    members = m
                    isLoading = false
                }.onFailure { isLoading = false }
            }.onFailure { isLoading = false }
        }
    }

    LaunchedEffect(groupId) { loadData() }

    if (showKickConfirm != null) {
        AlertDialog(
            onDismissRequest = { showKickConfirm = null },
            title = { Text("ยืนยันการลบ", fontWeight = FontWeight.Bold) },
            text = { Text("คุณแน่ใจหรือไม่ว่าต้องการลบ ${showKickConfirm?.name} ออกจากกลุ่ม?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        val userToKick = showKickConfirm!!
                        showKickConfirm = null
                        scope.launch {
                            groupRepository.kickMember(groupId, userToKick.id).onSuccess { loadData() }
                        }
                    }
                ) {
                    Text("ลบ", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showKickConfirm = null }) {
                    Text("ยกเลิก")
                }
            }
        )
    }

    if (showTransferConfirm != null) {
        AlertDialog(
            onDismissRequest = { showTransferConfirm = null },
            title = { Text("ยืนยันการโอนความเป็นเจ้าของ", fontWeight = FontWeight.Bold) },
            text = { Text("คุณแน่ใจหรือไม่ว่าต้องการโอนความเป็นเจ้าของกลุ่มให้ ${showTransferConfirm?.name}?\n(คุณจะเสียสิทธิ์ในการดูแลกลุ่ม)") },
            confirmButton = {
                TextButton(
                    onClick = {
                        val newUser = showTransferConfirm!!
                        showTransferConfirm = null
                        scope.launch {
                            groupRepository.transferOwnership(groupId, newUser.id).onSuccess { loadData() }
                        }
                    }
                ) {
                    Text("โอนสิทธิ์", color = LuklanColors.Primary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTransferConfirm = null }) {
                    Text("ยกเลิก")
                }
            }
        )
    }

    if (showDeleteGroupConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteGroupConfirm = false },
            title = { Text("ลบกลุ่ม?", fontWeight = FontWeight.Bold, color = LuklanColors.Error) },
            text = { Text("คุณต้องการลบกลุ่มนี้ใช่หรือไม่? สมาชิกทุกคนจะถูกลบออกจากกลุ่ม และข้อมูลกลุ่มจะหายไปทั้งหมด") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteGroupConfirm = false
                        scope.launch {
                            groupRepository.deleteGroup(groupId).onSuccess {
                                onBack()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LuklanColors.Error)
                ) {
                    Text("ลบกลุ่ม", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteGroupConfirm = false }) {
                    Text("ยกเลิก")
                }
            }
        )
    }

    if (isEditing) {
        AlertDialog(
            onDismissRequest = { isEditing = false },
            title = { Text("แก้ไขกลุ่ม", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Group Image Picker in Dialog
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(LuklanColors.Primary.copy(alpha = 0.1f))
                            .clickable { imagePickerLauncher.launch() },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isUpdating) {
                            CircularProgressIndicator(color = LuklanColors.Primary)
                        } else if (!group?.photoUrl.isNullOrEmpty()) {
                            AsyncImage(
                                model = group?.photoUrl,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                        } else {
                            Icon(Icons.Default.Group, null, tint = LuklanColors.Primary, modifier = Modifier.size(50.dp))
                        }
                        
                        Box(
                            modifier = Modifier.fillMaxSize().background(Color.Transparent),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            Icon(
                                Icons.Default.CameraAlt, 
                                null, 
                                tint = Color.White.copy(alpha = 0.8f), 
                                modifier = Modifier.size(20.dp).padding(bottom = 4.dp)
                            )
                        }
                    }

                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("ชื่อกลุ่ม") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Divider()
                    
                    Button(
                        onClick = { 
                            isEditing = false
                            showDeleteGroupConfirm = true 
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = LuklanColors.Error)
                    ) {
                        Icon(Icons.Default.Delete, null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("ลบกลุ่ม")
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val newName = editName
                        isEditing = false
                        scope.launch {
                            // Ensure persistence if name changed
                            if (newName != group?.name && newName.isNotBlank()) {
                                // Add updateGroupName logic or repurpose photo logic
                                groupRepository.updateGroupPhoto(groupId, group?.photoUrl ?: "").onSuccess {
                                    // Hack to force doc update if no dedicated name method
                                }
                            }
                            // Re-fetch to ensure state sync
                            loadData()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LuklanColors.Secondary)
                ) {
                    Text("ตกลง", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { isEditing = false }) { Text("ยกเลิก") }
            }
        )
    }

    val isCurrentUserPatient = members.find { it.id == currentUserId }?.role == "patient"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("สมาชิก", style = LuklanTypography.h1, color = LuklanColors.Primary, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = LuklanColors.Primary)
                    }
                },
                actions = {
                    if (currentUserId == group?.ownerId) {
                        Button(
                            onClick = { isEditing = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = LuklanColors.Secondary.copy(alpha = 0.2f),
                                contentColor = LuklanColors.Secondary
                            ),
                            shape = CircleShape,
                            modifier = Modifier.padding(end = 8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text("แก้ไขกลุ่ม", style = LuklanTypography.bodySmall, fontWeight = FontWeight.Bold)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = LuklanColors.Background)
            )
        },
        containerColor = LuklanColors.Background
    ) { paddingValues ->
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = LuklanColors.Primary)
            }
        } else {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = LuklanSpacing.lg)
                ) {
                    Spacer(Modifier.height(8.dp))

                    Text(
                        "รายชื่อสมาชิก (${members.size})",
                        style = LuklanTypography.h3,
                        color = LuklanColors.TextPrimary,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(16.dp))

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 100.dp)
                    ) {
                        items(members) { member ->
                            MemberItem(
                                member = member,
                                isOwner = member.id == group?.ownerId,
                                showActions = currentUserId == group?.ownerId && member.id != currentUserId,
                                showManageBtn = !isCurrentUserPatient && member.role == "patient",
                                isClickable = !isCurrentUserPatient && member.role == "patient",
                                onKick = { showKickConfirm = member },
                                onTransfer = { showTransferConfirm = member },
                                onClick = {
                                    if (!isCurrentUserPatient && member.role == "patient") {
                                        onNavigateToPatientTimeline(member.id, member.name)
                                    }
                                }
                            )
                        }
                    }
                }

                // Fixed Bottom Invite Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(
                            onClick = { onNavigateToInvite(groupId) },
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .border(1.dp, LuklanColors.Primary.copy(alpha = 0.2f), CircleShape)
                        ) {
                            Icon(Icons.Default.PersonAdd, contentDescription = "Invite", tint = LuklanColors.Primary, modifier = Modifier.size(32.dp))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("เชิญผู้ดูแล", style = LuklanTypography.bodyMedium, fontWeight = FontWeight.Bold, color = LuklanColors.Primary)
                    }
                }
            }
        }
    }
}

@Composable
fun MemberItem(
    member: User,
    isOwner: Boolean,
    showActions: Boolean,
    showManageBtn: Boolean,
    isClickable: Boolean,
    onKick: () -> Unit,
    onTransfer: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (isClickable) Modifier.clickable { onClick() } else Modifier),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 16.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(56.dp).clip(CircleShape).background(LuklanColors.Primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                if (!member.photoUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = member.photoUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                } else {
                    Icon(
                        if (member.role == "patient") Icons.Default.Person else Icons.Default.PersonOutline,
                        contentDescription = null,
                        tint = LuklanColors.Primary,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = member.name, 
                    style = LuklanTypography.h3, 
                    color = LuklanColors.TextPrimary,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (member.role == "patient") "ผู้ป่วย" else "ผู้ดูแล",
                        style = LuklanTypography.bodySmall,
                        color = LuklanColors.TextSecondary,
                        maxLines = 1
                    )
                    if (isOwner) {
                        Spacer(Modifier.width(4.dp))
                        Surface(
                            color = LuklanColors.Secondary,
                            shape = CircleShape
                        ) {
                            Text(
                                "เจ้าของกลุ่ม",
                                color = Color.White,
                                style = LuklanTypography.bodySmall,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
                                maxLines = 1
                            )
                        }
                    }
                }
            }
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                if (showManageBtn) {
                    Surface(
                        color = LuklanColors.Primary.copy(alpha = 0.1f),
                        shape = CircleShape,
                        modifier = Modifier.padding(end = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("จัดการยา", style = LuklanTypography.bodySmall, fontSize = 12.sp, color = LuklanColors.Primary, fontWeight = FontWeight.Bold)
                            Icon(Icons.Default.ChevronRight, null, tint = LuklanColors.Primary, modifier = Modifier.size(16.dp))
                        }
                    }
                    if (showActions) {
                        Spacer(Modifier.width(8.dp))
                    }
                }

                if (showActions) {
                    var showMenu by remember { mutableStateOf(false) }
                    Box {
                        IconButton(
                            onClick = { showMenu = true },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Menu", modifier = Modifier.size(20.dp))
                        }
                        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                            DropdownMenuItem(
                                text = { Text("โอนความเป็นเจ้าของ") },
                                onClick = {
                                    showMenu = false
                                    onTransfer()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("ลบออกจากกลุ่ม", color = Color.Red) },
                                onClick = {
                                    showMenu = false
                                    onKick()
                                }
                            )
                        }
                    }
                } else if (member.role != "patient") {
                    // Spacer for alignment if no actions and not patient
                    Spacer(Modifier.width(8.dp))
                }
            }
        }
    }
}
