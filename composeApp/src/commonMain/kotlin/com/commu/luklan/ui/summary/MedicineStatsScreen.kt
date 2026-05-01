package com.commu.luklan.ui.summary

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import com.commu.luklan.data.Medicine
import com.commu.luklan.ui.components.MonthYearPicker
import com.commu.luklan.ui.theme.LuklanColors
import com.commu.luklan.ui.theme.LuklanTheme.LuklanTypography
import kotlinx.datetime.*

val medChartColors = listOf(
    Color(0xFF33658A), // Primary Blue
    Color(0xFFF7AE2C), // Secondary Orange
    Color(0xFF4B9C1F), // Success Green
    Color(0xFFF26419), // Deep Orange
    Color(0xFF86BBD8), // Light Blue
    Color(0xFF2F4858), // Dark Blue
    Color(0xFF06D6A0), // Teal
    Color(0xFFFFD166), // Yellow
    Color(0xFFEF476F), // Pink
    Color(0xFF118AB2), // Blue
    Color(0xFF073B4C)  // Midnight
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicineStatsScreen(
    userId: String,
    initialMonth: Int,
    initialYear: Int,
    onBack: () -> Unit
) {
    val viewModel: SummaryViewModel = viewModel { SummaryViewModel() }
    val state by viewModel.state.collectAsState()

    var selectedMonth by remember { mutableStateOf(initialMonth) }
    var selectedYear by remember { mutableStateOf(initialYear) }
    var showMonthPicker by remember { mutableStateOf(false) }

    val thaiMonths = listOf("มกราคม", "กุมภาพันธ์", "มีนาคม", "เมษายน", "พฤษภาคม", "มิถุนายน", "กรกฎาคม", "สิงหาคม", "กันยายน", "ตุลาคม", "พฤศจิกายน", "ธันวาคม")

    LaunchedEffect(userId, selectedMonth, selectedYear) {
        viewModel.fetchMonthlyAdherence(userId, selectedMonth, selectedYear)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("สถิติการใช้ยารายวัน", style = LuklanTypography.h1, color = LuklanColors.Primary) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = LuklanColors.Primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = LuklanColors.Background)
            )
        },
        containerColor = LuklanColors.Background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Month Picker Section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .clickable { showMonthPicker = true },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "${thaiMonths[selectedMonth - 1]} ${selectedYear + 543}",
                    style = LuklanTypography.h2,
                    color = LuklanColors.Primary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.width(8.dp))
                Icon(Icons.Default.KeyboardArrowDown, null, tint = LuklanColors.Primary, modifier = Modifier.size(28.dp))
            }

            if (showMonthPicker) {
                MonthYearPicker(
                    initialMonth = selectedMonth,
                    initialYear = selectedYear,
                    onDismiss = { showMonthPicker = false },
                    onConfirm = { m, y ->
                        selectedMonth = m
                        selectedYear = y
                        showMonthPicker = false
                    }
                )
            }

            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxWidth().height(300.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = LuklanColors.Primary)
                }
            } else if (state.medicines.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().height(300.dp), contentAlignment = Alignment.Center) {
                    Text("ไม่พบข้อมูลยา", style = LuklanTypography.bodyLarge, color = LuklanColors.TextSecondary)
                }
            } else {
                // Stacked Bar Chart Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "สถิติการใช้ยาของเดือนที่เลือก",
                            style = LuklanTypography.h3,
                            color = LuklanColors.Primary
                        )
                        Spacer(Modifier.height(12.dp))
                        
                        MonthlyStackedChart(
                            medicines = state.medicines,
                            month = selectedMonth,
                            year = selectedYear
                        )
                        
                        Spacer(Modifier.height(24.dp))
                        
                        // Legend
                        Text(
                            text = "รายการยา",
                            style = LuklanTypography.bodyMedium,
                            color = LuklanColors.TextSecondary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(12.dp))
                        
                        MedicineLegend(state.medicines)
                    }
                }
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}

data class SelectedMedDetail(
    val medName: String,
    val date: String,
    val taken: Int,
    val total: Int,
    val color: Color
)

@Composable
fun MonthlyStackedChart(medicines: List<Medicine>, month: Int, year: Int) {
    val nextMonth = if (month == 12) LocalDate(year + 1, 1, 1) else LocalDate(year, month + 1, 1)
    val lastDay = try {
        nextMonth.minus(DatePeriod(days = 1)).dayOfMonth
    } catch (e: Exception) {
        31
    }

    var selectedDetail by remember { mutableStateOf<SelectedMedDetail?>(null) }
    val lazyListState = rememberLazyListState()

    // Chart scale
    var maxDosesInADay = 0
    val dailyData = (1..lastDay).map { day ->
        val dateStr = "${year}-${month.toString().padStart(2, '0')}-${day.toString().padStart(2, '0')}"
        val medStats = medicines.map { med ->
            val taken = med.times.count { time -> med.takenHistory.containsKey("${dateStr}_$time") }
            val total = if (med.startDate.isEmpty() || dateStr >= med.startDate) med.times.size else 0
            Pair(taken, total)
        }
        val totalTakenDoses = medStats.sumOf { it.first }
        if (totalTakenDoses > maxDosesInADay) maxDosesInADay = totalTakenDoses
        medStats
    }

    val chartHeight = 200.dp
    val steps = if (maxDosesInADay > 0) maxDosesInADay else 1
    val scaleFactor = chartHeight / steps.toFloat()

    Column {
        Text(
            text = "จำนวนใช้ยา (ครั้ง)",
            style = LuklanTypography.caption,
            color = LuklanColors.TextSecondary
        )
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(chartHeight + 40.dp)
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { selectedDetail = null })
                }
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.Bottom
            ) {
                // Y-Axis
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(bottom = 25.dp)
                        .offset(y = 12.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.End
                ) {
                    for (i in steps downTo 0) {
                        Text(
                            text = i.toString(),
                            style = LuklanTypography.caption.copy(fontSize = 10.sp),
                            color = LuklanColors.TextSecondary,
                            modifier = Modifier.width(12.dp),
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }

                Spacer(Modifier.width(8.dp))

                // Scrollable Content
                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    // Grid Lines
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 40.dp), // Align with bars area
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        for (i in steps downTo 0) {
                            HorizontalDivider(
                                color = LuklanColors.TextSecondary.copy(alpha = 0.1f),
                                thickness = 1.dp,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    LazyRow(
                        state = lazyListState,
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.Bottom,
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        items(lastDay) { index ->
                            val day = index + 1
                            val medStats = dailyData[index]
                            
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Bottom,
                                modifier = Modifier.fillMaxHeight()
                            ) {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(1.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.weight(1f, fill = false)
                                ) {
                                    medStats.forEachIndexed { medIndex, stat ->
                                        val takenCount = stat.first
                                        val totalCount = stat.second
                                        if (takenCount > 0) {
                                            val medName = medicines[medIndex].name
                                            val color = medChartColors[medIndex % medChartColors.size]
                                            val h = scaleFactor * takenCount.toFloat()
                                            
                                            val isSelected = selectedDetail != null && selectedDetail!!.medName == medName
                                            val targetAlpha = if (selectedDetail == null || isSelected) 1f else 0.2f
                                            val animatedAlpha by animateFloatAsState(
                                                targetValue = targetAlpha,
                                                animationSpec = tween(durationMillis = 150)
                                            )

                                            Box(
                                                modifier = Modifier
                                                    .size(width = 12.dp, height = h)
                                                    .clip(RoundedCornerShape(2.dp))
                                                    .background(color.copy(alpha = animatedAlpha))
                                                    .clickable {
                                                        val detail = SelectedMedDetail(
                                                            medName = medName,
                                                            date = "${day} ${getMonthNameThai(month)} ${year + 543}",
                                                            taken = takenCount,
                                                            total = totalCount,
                                                            color = color
                                                        )
                                                        selectedDetail = if (selectedDetail?.medName == detail.medName && 
                                                            selectedDetail?.date == detail.date) null else detail
                                                    }
                                            )
                                        }
                                    }
                                }
                                
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = day.toString(),
                                    style = LuklanTypography.caption.copy(fontSize = 10.sp),
                                    color = LuklanColors.TextSecondary,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
        
        Text(
            text = "วันที่",
            style = LuklanTypography.caption,
            color = LuklanColors.TextSecondary,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(16.dp))

        // Fixed Info Area Below Graph
        AnimatedVisibility(
            visible = selectedDetail != null,
            enter = fadeIn(animationSpec = tween(150)),
            exit = fadeOut(animationSpec = tween(150))
        ) {
            selectedDetail?.let { detail ->
                Surface(
                    color = detail.color.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(detail.color))
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                text = detail.medName,
                                style = LuklanTypography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = detail.color
                            )
                            Text(
                                text = "ใช้วันที่ ${detail.date} • ${detail.taken}/${detail.total} ครั้ง",
                                style = LuklanTypography.bodySmall,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }
    }
}

fun getMonthNameThai(month: Int): String {
    val thaiMonths = listOf("มกราคม", "กุมภาพันธ์", "มีนาคม", "เมษายน", "พฤษภาคม", "มิถุนายน", "กรกฎาคม", "สิงหาคม", "กันยายน", "ตุลาคม", "พฤศจิกายน", "ธันวาคม")
    return if (month in 1..12) thaiMonths[month - 1] else ""
}

@Composable
fun MedicineLegend(medicines: List<Medicine>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        medicines.chunked(2).forEach { rowMeds ->
            Row(modifier = Modifier.fillMaxWidth()) {
                rowMeds.forEachIndexed { index, medicine ->
                    val medIndex = medicines.indexOf(medicine)
                    val color = medChartColors[medIndex % medChartColors.size]
                    
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(color)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = medicine.name,
                            style = LuklanTypography.bodySmall,
                            color = LuklanColors.TextPrimary,
                            maxLines = 1
                        )
                    }
                }
                if (rowMeds.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}
