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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.QrCodeScanner
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
import com.commu.luklan.data.getAuthRepository
import com.commu.luklan.data.getGroupRepository
import com.commu.luklan.ui.theme.*
import com.commu.luklan.ui.theme.LuklanTheme.LuklanTypography
import coil3.compose.AsyncImage
import kotlinx.coroutines.launch
import com.commu.luklan.data.AppCache

import androidx.compose.material3.pulltorefresh.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaretakerDashboardScreen(
    onBack: () -> Unit,
    onRefresh: (() -> Unit)? = null,
    onNavigateToJoin: () -> Unit,
    onNavigateToCreate: () -> Unit,
    onNavigateToMembers: (String, String) -> Unit
) {
    val groupRepository = remember { getGroupRepository() }
    val authRepository = remember { getAuthRepository() }
    val userId = remember { authRepository.getCurrentUserId() ?: "" }
    val scope = rememberCoroutineScope()
    
    var groups by remember { mutableStateOf<List<CareGroup>>(AppCache.groupsCache[userId] ?: emptyList()) }
    var isLoading by remember { mutableStateOf(groups.isEmpty()) }
    
    var isRefreshing by remember { mutableStateOf(false) }
    val pullToRefreshState = rememberPullToRefreshState()

    fun loadGroups() {
        if (userId.isNotEmpty()) {
            scope.launch {
                groupRepository.getGroupsForUser(userId).onSuccess {
                    groups = it
                    AppCache.groupsCache[userId] = it
                    isLoading = false
                    isRefreshing = false
                }.onFailure {
                    isLoading = false
                    isRefreshing = false
                }
            }
        }
    }

    LaunchedEffect(Unit) { loadGroups() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("กลุ่มผู้ดูแล", style = LuklanTypography.h1, color = LuklanColors.Primary, fontWeight = FontWeight.Bold) },
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
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            state = pullToRefreshState,
            onRefresh = {
                isRefreshing = true
                onRefresh?.invoke()
                loadGroups()
            },
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            indicator = {
                PullToRefreshDefaults.Indicator(
                    state = pullToRefreshState,
                    isRefreshing = isRefreshing,
                    modifier = Modifier.align(Alignment.TopCenter),
                    containerColor = Color.White,
                    color = LuklanColors.Primary
                )
            }
        ) {
            if (isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = LuklanColors.Primary)
                }
            } else if (groups.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(LuklanSpacing.lg),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Spacer(modifier = Modifier.height(100.dp))
                    Text(
                        "คุณยังไม่มีกลุ่ม",
                        style = LuklanTypography.h3,
                        color = LuklanColors.TextSecondary
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(32.dp)) {
                        AddGroupButton(onClick = onNavigateToJoin, label = "เข้าร่วมกลุ่ม", icon = Icons.Default.QrCodeScanner)
                        AddGroupButton(onClick = onNavigateToCreate, label = "สร้างกลุ่มใหม่")
                    }
                    Spacer(modifier = Modifier.height(100.dp))
                }
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(LuklanSpacing.lg),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(groups) { group ->
                            GroupItem(group, onClick = { onNavigateToMembers(group.id, group.name) })
                        }
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(LuklanSpacing.lg), 
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AddGroupButton(onClick = onNavigateToJoin, label = "เข้าร่วมกลุ่ม", icon = Icons.Default.QrCodeScanner)
                        AddGroupButton(onClick = onNavigateToCreate, label = "สร้างกลุ่มใหม่")
                    }
                }
            }
        }
    }
}

@Composable
fun AddGroupButton(onClick: () -> Unit, label: String, icon: androidx.compose.ui.graphics.vector.ImageVector = Icons.Default.Add) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(
            onClick = onClick,
            modifier = Modifier.size(64.dp).clip(CircleShape).background(Color.White).border(1.dp, LuklanColors.Primary.copy(alpha = 0.2f), CircleShape)
        ) {
            Icon(icon, contentDescription = label, tint = LuklanColors.Primary, modifier = Modifier.size(40.dp))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(label, style = LuklanTypography.bodyMedium, fontWeight = FontWeight.Bold, color = LuklanColors.Primary)
    }
}

@Composable
fun GroupItem(group: CareGroup, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .border(1.dp, LuklanColors.Primary.copy(alpha = 0.1f), RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(24.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(LuklanColors.Primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                if (!group.photoUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = group.photoUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                } else {
                    Icon(Icons.Default.Group, contentDescription = null, tint = LuklanColors.Primary, modifier = Modifier.size(36.dp))
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(text = group.name, style = LuklanTypography.h2, color = LuklanColors.TextPrimary, fontWeight = FontWeight.Bold)
                Text(text = "รหัสเชิญ: ${group.inviteCode}", style = LuklanTypography.bodySmall, color = LuklanColors.TextSecondary)
            }
            
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = LuklanColors.TextSecondary)
        }
    }
}
