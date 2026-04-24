package com.commu.luklan.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
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
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@Composable
fun MonthYearPicker(
    initialMonth: Int,
    initialYear: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int, Int) -> Unit
) {
    var tempMonth by remember { mutableStateOf(initialMonth) }
    var tempYear by remember { mutableStateOf(initialYear) }
    
    val thaiMonthsShort = listOf("ม.ค.", "ก.พ.", "มี.ค.", "เม.ย.", "พ.ค.", "มิ.ย.", "ก.ค.", "ส.ค.", "ก.ย.", "ต.ค.", "พ.ย.", "ธ.ค.")
    val thaiMonthsFull = listOf("มกราคม", "กุมภาพันธ์", "มีนาคม", "เมษายน", "พฤษภาคม", "มิถุนายน", "กรกฎาคม", "สิงหาคม", "กันยายน", "ตุลาคม", "พฤศจิกายน", "ธันวาคม")

    val now = remember {
        val instant = kotlinx.datetime.Instant.fromEpochMilliseconds(com.commu.luklan.utils.getCurrentTimeMillis())
        instant.toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault())
    }
    
    val isRealCurrentYear = tempYear == now.year

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FBFF)),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
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
                    IconButton(onClick = { tempYear-- }) {
                        Icon(Icons.Default.ChevronLeft, null, tint = LuklanColors.Primary)
                    }
                    
                    Text(
                        text = "${thaiMonthsFull[tempMonth - 1]} ${tempYear + 543}",
                        style = LuklanTypography.h3,
                        fontWeight = FontWeight.Bold,
                        color = LuklanColors.Primary
                    )
                    
                    IconButton(onClick = { tempYear++ }) {
                        Icon(Icons.Default.ChevronRight, null, tint = LuklanColors.Primary)
                    }
                }
                
                Divider(modifier = Modifier.padding(vertical = 12.dp), color = LuklanColors.Primary.copy(alpha = 0.1f))
                
                // Month Grid 4x3
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.height(300.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(thaiMonthsShort) { index, m ->
                        val isSelected = tempMonth == index + 1
                        val isRealTodayMonth = isRealCurrentYear && (index + 1 == now.monthNumber)
                        
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .clickable { 
                                    tempMonth = index + 1
                                    onConfirm(tempMonth, tempYear)
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
            }
        }
    }
}
