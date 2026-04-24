package com.commu.luklan.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.commu.luklan.ui.theme.LuklanColors
import com.commu.luklan.ui.theme.LuklanTypography
import kotlinx.datetime.*
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@Composable
fun FullDatePicker(
    initialDate: String, // yyyy-MM-dd
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val dateParts = initialDate.split("-")
    var tempYear by remember { mutableStateOf(if (dateParts.size == 3) dateParts[0].toIntOrNull() ?: 2024 else 2024) }
    var tempMonth by remember { mutableStateOf(if (dateParts.size == 3) dateParts[1].toIntOrNull() ?: 1 else 1) }
    var tempDay by remember { mutableStateOf(if (dateParts.size == 3) dateParts[2].toIntOrNull() ?: 1 else 1) }
    
    var step by remember { mutableStateOf(1) } // 1: Month/Year, 2: Day
    
    val thaiMonthsFull = listOf("มกราคม", "กุมภาพันธ์", "มีนาคม", "เมษายน", "พฤษภาคม", "มิถุนายน", "กรกฎาคม", "สิงหาคม", "กันยายน", "ตุลาคม", "พฤศจิกายน", "ธันวาคม")
    val thaiMonthsShort = listOf("ม.ค.", "ก.พ.", "มี.ค.", "เม.ย.", "พ.ค.", "มิ.ย.", "ก.ค.", "ส.ค.", "ก.ย.", "ต.ค.", "พ.ย.", "ธ.ค.")
    val dayInitials = listOf("อา", "จ", "อ", "พ", "พฤ", "ศ", "ส")

    val now = remember {
        val instant = kotlinx.datetime.Instant.fromEpochMilliseconds(com.commu.luklan.utils.getCurrentTimeMillis())
        instant.toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault())
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FBFF)),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { if (step == 1) tempYear-- else step = 1 }) {
                        Icon(Icons.Default.ChevronLeft, null, tint = LuklanColors.Primary)
                    }
                    
                    Text(
                        text = "${thaiMonthsFull[tempMonth - 1]} ${tempYear + 543}",
                        style = LuklanTypography.h3,
                        fontWeight = FontWeight.Bold,
                        color = LuklanColors.Primary
                    )
                    
                    IconButton(onClick = { if (step == 1) tempYear++ }, enabled = step == 1) {
                        Icon(Icons.Default.ChevronRight, null, tint = if (step == 1) LuklanColors.Primary else Color.Transparent)
                    }
                }
                
                Divider(modifier = Modifier.padding(vertical = 12.dp), color = LuklanColors.Primary.copy(alpha = 0.1f))
                
                if (step == 1) {
                    // Month Grid
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier = Modifier.height(300.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        itemsIndexed(thaiMonthsShort) { index, m ->
                            val monthNum = index + 1
                            val isSelected = tempMonth == monthNum
                            val isRealTodayMonth = (tempYear == now.year) && (monthNum == now.monthNumber)
                            
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(64.dp)
                                    .clickable { 
                                        tempMonth = monthNum
                                        step = 2
                                    },
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = m,
                                    style = LuklanTypography.h3,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) LuklanColors.Secondary else LuklanColors.Primary
                                )
                                if (isRealTodayMonth) {
                                    Box(
                                        modifier = Modifier
                                            .padding(top = 2.dp)
                                            .size(5.dp)
                                            .clip(CircleShape)
                                            .background(if (isSelected) LuklanColors.Secondary else LuklanColors.Primary)
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // Day Grid (Calendar)
                    val firstDayOfMonth = LocalDate(tempYear, tempMonth, 1)
                    val dayOfWeek = (firstDayOfMonth.dayOfWeek.ordinal + 1) % 7 // 0: Sun, 1: Mon...
                    
                    val daysInMonth = try {
                        val firstDayOfNextMonth = if (tempMonth == 12) LocalDate(tempYear + 1, 1, 1) else LocalDate(tempYear, tempMonth + 1, 1)
                        firstDayOfNextMonth.minus(DatePeriod(days = 1)).dayOfMonth
                    } catch (e: Exception) { 31 }

                    // Weekday initials
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                        dayInitials.forEach {
                            Text(it, style = LuklanTypography.bodySmall, color = LuklanColors.Primary.copy(0.5f), fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(Modifier.height(8.dp))

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(7),
                        modifier = Modifier.height(240.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Empty slots for alignment
                        items(dayOfWeek) { Box(Modifier.size(40.dp)) }
                        
                        items((1..daysInMonth).toList()) { d ->
                            val isSelected = tempDay == d
                            val isRealTodayDay = (tempYear == now.year && tempMonth == now.monthNumber && d == now.dayOfMonth)

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1f)
                                    .clickable { 
                                        tempDay = d
                                        val finalDate = "$tempYear-${tempMonth.toString().padStart(2, '0')}-${tempDay.toString().padStart(2, '0')}"
                                        onConfirm(finalDate)
                                    },
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = d.toString(),
                                    style = LuklanTypography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) LuklanColors.Secondary else LuklanColors.Primary
                                )
                                if (isRealTodayDay) {
                                    Box(
                                        modifier = Modifier
                                            .padding(top = 2.dp)
                                            .size(4.dp)
                                            .clip(CircleShape)
                                            .background(if (isSelected) LuklanColors.Secondary else LuklanColors.Primary)
                                    )
                                }
                            }
                        }
                    }
                }
                
                Spacer(Modifier.height(16.dp))
                
                TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                    Text("ยกเลิก", style = LuklanTypography.bodyLarge, color = LuklanColors.TextSecondary)
                }
            }
        }
    }
}
