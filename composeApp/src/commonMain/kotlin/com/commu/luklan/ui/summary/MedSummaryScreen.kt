package com.commu.luklan.ui.summary

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.commu.luklan.ui.components.MonthYearPicker
import com.commu.luklan.ui.theme.LuklanColors
import com.commu.luklan.ui.theme.LuklanTheme.LuklanTypography
import com.commu.luklan.utils.getCurrentTimeMillis
import kotlinx.datetime.*
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class)
@Composable
fun MedSummaryScreen(
    userId: String,
    userName: String? = null,
    onNavigateToStats: (Int, Int) -> Unit,
    onBack: () -> Unit
) {
    val viewModel: SummaryViewModel = viewModel { SummaryViewModel() }
    val state by viewModel.state.collectAsState()

    val now = remember {
        val nowMillis = getCurrentTimeMillis()
        val instant = Instant.fromEpochMilliseconds(nowMillis)
        instant.toLocalDateTime(TimeZone.currentSystemDefault())
    }

    var selectedMonth by remember { mutableStateOf(now.monthNumber) }
    var selectedYear by remember { mutableStateOf(now.year) }
    var showMonthPicker by remember { mutableStateOf(false) }

    val thaiMonths = listOf("มกราคม", "กุมภาพันธ์", "มีนาคม", "เมษายน", "พฤษภาคม", "มิถุนายน", "กรกฎาคม", "สิงหาคม", "กันยายน", "ตุลาคม", "พฤศจิกายน", "ธันวาคม")

    LaunchedEffect(userId, selectedMonth, selectedYear) {
        viewModel.fetchMonthlyAdherence(userId, selectedMonth, selectedYear)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("สถิติการใช้ยา", style = LuklanTypography.h1, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = LuklanColors.Primary)
            )
        },
        containerColor = LuklanColors.Primary
    ) { padding ->
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color.White)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (userName != null) {
                    Text(
                        text = "สถิติการใช้ยาของคุณ $userName",
                        style = LuklanTypography.h2,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 8.dp),
                        textAlign = TextAlign.Center
                    )
                } else {
                    Text(
                        text = "สถิติการใช้ยาของคุณ",
                        style = LuklanTypography.h2,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                // Month Picker Trigger
                Row(
                    modifier = Modifier
                        .padding(bottom = 32.dp)
                        .clickable { showMonthPicker = true },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${thaiMonths[selectedMonth - 1]} ${selectedYear + 543}",
                        style = LuklanTypography.bodyLarge,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                    Spacer(Modifier.width(8.dp))
                    Icon(Icons.Default.KeyboardArrowDown, null, tint = Color.White, modifier = Modifier.size(24.dp))
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

                // Circular Percentage Display
                Box(
                    modifier = Modifier
                        .size(220.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        progress = { (state.adherencePercentage / 100).toFloat() },
                        modifier = Modifier.size(190.dp),
                        color = LuklanColors.Secondary,
                        strokeWidth = 14.dp,
                        trackColor = Color.White.copy(alpha = 0.2f),
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "ใช้ยาสม่ำเสมอ",
                            style = LuklanTypography.bodySmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                        Text(
                            text = "${state.adherencePercentage.toInt()}%",
                            style = LuklanTypography.h1.copy(fontSize = 54.sp),
                            color = Color.White
                        )
                    }
                }

                Spacer(Modifier.height(40.dp))

                // Summary Message Card
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White.copy(alpha = 0.15f)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "ผลการประเมินการใช้ยา:",
                            style = LuklanTypography.bodyLarge,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = state.summaryMessage,
                            style = LuklanTypography.h3,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(Modifier.height(24.dp))
                        
                        Divider(color = Color.White.copy(alpha = 0.1f))
                        
                        Spacer(Modifier.height(24.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            SummaryStatItem(
                                label = "คุณใช้ยาทั้งหมด",
                                value = "${state.totalLogs}",
                                unit = "ครั้ง"
                            )
                            SummaryStatItem(
                                label = "จำนวนยาทั้งหมดที่ต้องใช้",
                                value = "${state.totalExpected}",
                                unit = "ครั้ง"
                            )
                        }
                    }
                }

                Spacer(Modifier.height(40.dp))
                
                Button(
                    onClick = { onNavigateToStats(selectedMonth, selectedYear) },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = LuklanColors.Primary)
                ) {
                    Text("ดูสถิติการใช้ยารายวัน", style = LuklanTypography.buttonLarge)
                }
            }
        }
    }
}

@Composable
fun SummaryStatItem(label: String, value: String, unit: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = LuklanTypography.caption, color = Color.White.copy(alpha = 0.6f), textAlign = TextAlign.Center)
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(text = value, style = LuklanTypography.h2, color = Color.White, fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(4.dp))
            Text(text = unit, style = LuklanTypography.bodySmall, color = Color.White.copy(alpha = 0.8f), modifier = Modifier.padding(bottom = 4.dp))
        }
    }
}
