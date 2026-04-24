package com.commu.luklan.ui.groups

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.commu.luklan.data.AuthRepository
import com.commu.luklan.data.Medicine
import com.commu.luklan.data.getMedicineRepository
import com.commu.luklan.ui.theme.*
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import luklan.composeapp.generated.resources.Res
import luklan.composeapp.generated.resources.*

data class MedicineGroup(
    val category: String,
    val count: Int,
    val medicines: List<Medicine>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicineGroupsScreen(
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
            val userId = authRepository.getCurrentUserId()
            if (userId != null) {
                medicineRepository
                    .getMedicines(userId)
                    .onSuccess { medicines ->
                        // Group medicines by category
                        val grouped = medicines
                            .groupBy { it.category }
                            .map { (category, meds) ->
                                MedicineGroup(
                                    category = category.ifEmpty { "อื่นๆ" },
                                    count = meds.size,
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
                        style = LuklanTypography.h3,
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
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator(
                            color = LuklanColors.Primary,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "กำลังโหลดข้อมูล...",
                            style = LuklanTypography.bodyLarge,
                            color = LuklanColors.TextSecondary
                        )
                    }
                }
            } else if (groups.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 32.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            Icons.Default.Category,
                            contentDescription = "No groups",
                            tint = LuklanColors.TextSecondary,
                            modifier = Modifier.size(64.dp)
                        )
                        Text(
                            text = "ยังไม่มียาในระบบ",
                            style = LuklanTypography.bodyLarge,
                            color = LuklanColors.TextSecondary
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(
                        start = LuklanSpacing.lg,
                        top = LuklanSpacing.md,
                        end = LuklanSpacing.lg,
                        bottom = LuklanSpacing.lg
                    )
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
            // Group Header
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
                        when (group.category) {
                            "แคปซูล" -> Image(painterResource(Res.drawable.capsule), null, modifier = Modifier.size(32.dp))
                            "เม็ด" -> Image(painterResource(Res.drawable.pill), null, modifier = Modifier.size(32.dp))
                            "น้ำ" -> Image(painterResource(Res.drawable.liquid), null, modifier = Modifier.size(32.dp))
                            "ครีม" -> Image(painterResource(Res.drawable.cream), null, modifier = Modifier.size(32.dp))
                            "เหน็บ" -> Image(painterResource(Res.drawable.suppository), null, modifier = Modifier.size(32.dp))
                            "ฉีด" -> Image(painterResource(Res.drawable.inject), null, modifier = Modifier.size(32.dp))
                            "อื่นๆ" -> Image(painterResource(Res.drawable.other), null, modifier = Modifier.size(32.dp))
                            else -> Icon(Icons.Default.Category, null, tint = LuklanColors.Primary, modifier = Modifier.size(28.dp))
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(LuklanSpacing.md))
                    
                    Column {
                        Text(
                            text = group.category,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = LuklanColors.TextPrimary
                        )
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
            
            // Medicine List (when expanded)
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
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = LuklanColors.Background),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(LuklanSpacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(32.dp),
                contentAlignment = Alignment.Center
            ) {
                when (medicine.category) {
                    "แคปซูล" -> Image(painterResource(Res.drawable.capsule), null, modifier = Modifier.fillMaxSize())
                    "เม็ด" -> Image(painterResource(Res.drawable.pill), null, modifier = Modifier.fillMaxSize())
                    "น้ำ" -> Image(painterResource(Res.drawable.liquid), null, modifier = Modifier.fillMaxSize())
                    "ครีม" -> Image(painterResource(Res.drawable.cream), null, modifier = Modifier.fillMaxSize())
                    "เหน็บ" -> Image(painterResource(Res.drawable.suppository), null, modifier = Modifier.fillMaxSize())
                    "ฉีด" -> Image(painterResource(Res.drawable.inject), null, modifier = Modifier.fillMaxSize())
                    "อื่นๆ" -> Image(painterResource(Res.drawable.other), null, modifier = Modifier.fillMaxSize())
                    else -> Text("💊", fontSize = 24.sp)
                }
            }
            
            Spacer(modifier = Modifier.width(LuklanSpacing.sm))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = medicine.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = LuklanColors.TextPrimary
                )
                
                val dosageText = buildString {
                    if (medicine.dosage.isNotEmpty()) {
                        append(medicine.dosage)
                        if (medicine.unit.isNotEmpty()) {
                            append(" ${medicine.unit}")
                        }
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
            
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = "View details",
                tint = LuklanColors.TextSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
