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
import com.commu.luklan.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupMembersScreen(
    groupId: String,
    groupName: String,
    onBack: () -> Unit,
    onNavigateToInvite: () -> Unit,
    onNavigateToPatientTimeline: (String, String) -> Unit
) {
    val groupRepository = remember { getGroupRepository() }
    val authRepository = remember { getAuthRepository() }
    val scope = rememberCoroutineScope()
    
    var group by remember { mutableStateOf<CareGroup?>(null) }
    var members by remember { mutableStateOf<List<User>>(emptyList()) }
    var currentUserId by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    var showKickConfirm by remember { mutableStateOf<User?>(null) }
    var showTransferConfirm by remember { mutableStateOf<User?>(null) }

    fun loadData() {
        scope.launch {
            currentUserId = authRepository.getCurrentUserId()
            groupRepository.getGroupById(groupId).onSuccess { g ->
                group = g
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("สมาชิก", style = LuklanTypography.h1, fontWeight = FontWeight.Bold) },
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
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = LuklanColors.Primary)
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(LuklanSpacing.lg),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(members) { member ->
                            MemberItem(
                                user = member,
                                isOwner = member.id == group?.ownerId,
                                canManage = currentUserId == group?.ownerId && member.id != currentUserId,
                                onClick = {
                                    if (member.role == "patient") {
                                        onNavigateToPatientTimeline(member.id, member.name)
                                    }
                                },
                                onKick = { showKickConfirm = member },
                                onTransfer = { showTransferConfirm = member }
                            )
                        }
                    }
                    
                    Box(modifier = Modifier.fillMaxWidth().padding(LuklanSpacing.lg), contentAlignment = Alignment.Center) {
                        AddMemberButton(onClick = onNavigateToInvite)
                    }
                }
            }
        }
    }
}

@Composable
fun AddMemberButton(onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(
            onClick = onClick,
            modifier = Modifier.size(64.dp).clip(CircleShape).background(Color.White).border(1.dp, LuklanColors.Primary.copy(alpha = 0.2f), CircleShape)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add", tint = LuklanColors.Primary, modifier = Modifier.size(40.dp))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text("เพิ่มสมาชิก", style = LuklanTypography.bodyLarge, fontWeight = FontWeight.Bold, color = LuklanColors.Primary)
    }
}

@Composable
fun MemberItem(
    user: User, 
    isOwner: Boolean, 
    canManage: Boolean,
    onClick: () -> Unit,
    onKick: () -> Unit,
    onTransfer: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, LuklanColors.Primary.copy(alpha = 0.1f), RoundedCornerShape(24.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(LuklanColors.Primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, contentDescription = null, tint = LuklanColors.Primary, modifier = Modifier.size(32.dp))
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = user.name, 
                        style = LuklanTypography.h2, 
                        color = LuklanColors.TextPrimary, 
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (isOwner) {
                        Spacer(Modifier.width(8.dp))
                        Surface(
                            color = LuklanColors.Secondary,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                "เจ้าของกลุ่ม", 
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = LuklanTypography.bodySmall,
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                        }
                    }
                }
                Text(
                    text = if (user.role == "patient") "ผู้ป่วย" else "ผู้ดูแล", 
                    style = LuklanTypography.bodySmall, 
                    color = LuklanColors.TextSecondary
                )
            }
            
            if (canManage) {
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menu", tint = LuklanColors.TextSecondary)
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier.background(Color.White)
                    ) {
                        DropdownMenuItem(
                            text = { Text("โอนความเป็นเจ้าของ", color = LuklanColors.TextPrimary, fontWeight = FontWeight.Bold) },
                            onClick = { 
                                onTransfer()
                                showMenu = false 
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("ลบออกจากกลุ่ม", color = Color.Red, fontWeight = FontWeight.Bold) },
                            onClick = { 
                                onKick()
                                showMenu = false 
                            }
                        )
                    }
                }
            } else if (user.role == "patient") {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = LuklanColors.Primary
                ) {
                    Text(
                        "ดูข้อมูล", 
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = LuklanTypography.bodySmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
