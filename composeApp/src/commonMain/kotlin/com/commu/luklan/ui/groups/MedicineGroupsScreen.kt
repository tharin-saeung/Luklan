package com.commu.luklan.ui.groups

import androidx.compose.foundation.background
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
import com.commu.luklan.data.Medicine
import com.commu.luklan.data.getMedicineRepository
import com.commu.luklan.ui.theme.*
import com.commu.luklan.ui.components.MedicineIcon
import kotlinx.coroutines.launch
import com.commu.luklan.ui.theme.LuklanTheme.LuklanTypography

data class MedicineGroup(
    val category: String,
    val count: Int,
    val outOfStockCount: Int,
    val medicines: List<Medicine>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicineGroupsScreen(
    targetUserId: String? = null,
    onBack: () -> Unit,
    onMedicineClick: (Medicine) -> Unit
) {
    val medicineRepository = remember { getMedicineRepository() }
    val authRepository = remember { AuthRepository() }
    val scope = rememberCoroutineScope()
    var groups by remember { mutableStateOf<List<MedicineGroup>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    fun loadGroups() {
        isLoading = true
        scope.launch {
            val userId = targetUserId ?: authRepository.getCurrentUserId()
            if (userId != null) {
                medicineRepository
                    .getMedicines(userId)
                    .onSuccess { medicines ->
                        val grouped = medicines
                            .groupBy { it.category }
                            .map { (category, meds) ->
                                val outOfStockCount = meds.count { 
                                    val amt = it.currentAmount.toDoubleOrNull() ?: 0.0
                                    val dose = it.dosage.toDoubleOrNull() ?: 0.0
                                    amt < dose 
                                }
                                MedicineGroup(
                                    category = category.ifEmpty { "อื่นๆ" },
                                    count = meds.size,
                                    outOfStockCount = outOfStockCount,
                                    medicines = meds.sortedBy { it.name }
                                )
                            }
                            .sortedWith(compareBy<MedicineGroup> { it.category == "อื่นๆ" || it.category == "ไม่ระบุหมวดหมู่" }
                                .thenByDescending { it.count })
                        
                        groups = grouped
                        isLoading = false
                    }
                    .onFailure { isLoading = false }
            } else {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) { loadGroups() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "กลุ่มยา",
                        style = LuklanTypography.h1,
                        color = LuklanColors.Primary,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = LuklanColors.Primary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* Add logic if needed */ }) {
                        Icon(Icons.Default.Add, contentDescription = "Add", tint = LuklanColors.Primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = LuklanColors.Background
                )
            )
        },
        containerColor = LuklanColors.Background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = LuklanColors.Primary)
                }
            } else if (groups.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "ยังไม่มียาในระบบ",
                        style = LuklanTypography.bodyLarge,
                        color = LuklanColors.TextSecondary
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(LuklanSpacing.lg)
                ) {
                    items(groups) { group ->
                        GroupCard(
                            group = group,
                            onMedicineClick = onMedicineClick
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GroupCard(
    group: MedicineGroup,
    onMedicineClick: (Medicine) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(LuklanSpacing.md)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                LuklanColors.Primary.copy(alpha = 0.05f),
                                RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        MedicineIcon(category = group.category, iconSize = 32.dp)
                    }
                    
                    Spacer(modifier = Modifier.width(LuklanSpacing.md))
                    
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = group.category,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = LuklanColors.TextPrimary
                            )
                            if (group.outOfStockCount > 0) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .size(22.dp)
                                        .clip(CircleShape)
                                        .background(Color.Red),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = group.outOfStockCount.toString(),
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        Text(
                            text = "${group.count} รายการ",
                            fontSize = 14.sp,
                            color = LuklanColors.TextSecondary
                        )
                    }
                }
                
                Icon(
                    if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = LuklanColors.Primary
                )
            }
            
            if (isExpanded) {
                Spacer(modifier = Modifier.height(LuklanSpacing.md))
                Divider(color = LuklanColors.Primary.copy(alpha = 0.1f))
                Spacer(modifier = Modifier.height(LuklanSpacing.sm))
                
                group.medicines.forEach { medicine ->
                    MedicineItemInGroup(
                        medicine = medicine,
                        onClick = { onMedicineClick(medicine) }
                    )
                    Spacer(modifier = Modifier.height(LuklanSpacing.sm))
                }
            }
        }
    }
}

@Composable
fun MedicineItemInGroup(
    medicine: Medicine,
    onClick: () -> Unit
) {
    val amount = medicine.currentAmount.toDoubleOrNull() ?: 0.0
    val dose = medicine.dosage.toDoubleOrNull() ?: 0.0
    val isOutOfStock = amount < dose

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isOutOfStock) Color.Red else LuklanColors.Background
        ),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(LuklanSpacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MedicineIcon(category = medicine.category, iconSize = 32.dp)
            
            Spacer(modifier = Modifier.width(LuklanSpacing.sm))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = medicine.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isOutOfStock) Color.White else LuklanColors.TextPrimary
                )
                
                if (isOutOfStock) {
                    Text(
                        text = "ยาหมดแล้ว",
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.9f),
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    val daysLeft = medicine.calculateDaysRemaining()
                    val dosageText = buildString {
                        if (medicine.dosage.isNotEmpty()) {
                            append(medicine.dosage)
                            if (medicine.unit.isNotEmpty()) {
                                append(" ${medicine.unit}")
                            }
                        }
                        if (daysLeft <= 7) {
                            append(" (เหลือ $daysLeft วัน)")
                        }
                    }
                    
                    if (dosageText.isNotEmpty()) {
                        Text(
                            text = dosageText,
                            fontSize = 13.sp,
                            color = LuklanColors.TextSecondary
                        )
                    }
                }
            }
            
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = "View details",
                tint = if (isOutOfStock) Color.White else LuklanColors.TextSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
