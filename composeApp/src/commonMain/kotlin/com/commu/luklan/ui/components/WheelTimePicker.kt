package com.commu.luklan.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import com.commu.luklan.ui.theme.LuklanTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import com.commu.luklan.ui.theme.LuklanTheme.LuklanColors

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WheelTimePicker(startTime: String = "08:00", onTimeSelected: (String) -> Unit) {
    val initialHour = startTime.split(":")[0].toIntOrNull() ?: 8
    val initialMinute = startTime.split(":")[1].toIntOrNull() ?: 0

    val hours = (0..23).toList()
    val minutes = (0..59).toList()

    val itemHeight = 60.dp
    val visibleItemsCount = 3
    val itemHeightPx = with(LocalDensity.current) { itemHeight.toPx() }

    val hourListState =
            rememberLazyListState(
                    initialFirstVisibleItemIndex =
                            (Int.MAX_VALUE / 2) - (Int.MAX_VALUE / 2) % hours.size - 1 + initialHour
            )
    val minuteListState =
            rememberLazyListState(
                    initialFirstVisibleItemIndex =
                            (Int.MAX_VALUE / 2) - (Int.MAX_VALUE / 2) % minutes.size - 1 +
                                    initialMinute
            )

    val hourFlingBehavior = rememberSnapFlingBehavior(lazyListState = hourListState)
    val minuteFlingBehavior = rememberSnapFlingBehavior(lazyListState = minuteListState)

    var selectedHour by remember { mutableStateOf(initialHour) }
    var selectedMinute by remember { mutableStateOf(initialMinute) }

    LaunchedEffect(hourListState) {
        snapshotFlow { hourListState.firstVisibleItemIndex }
                .map { index ->
                    val centerIndex = index + 1
                    hours[centerIndex % hours.size]
                }
                .distinctUntilChanged()
                .collect { hour ->
                    selectedHour = hour
                    onTimeSelected(
                            "${selectedHour.toString().padStart(2, '0')}:${selectedMinute.toString().padStart(2, '0')}"
                    )
                }
    }

    LaunchedEffect(minuteListState) {
        snapshotFlow { minuteListState.firstVisibleItemIndex }
                .map { index ->
                    val centerIndex = index + 1
                    minutes[centerIndex % minutes.size]
                }
                .distinctUntilChanged()
                .collect { minute ->
                    selectedMinute = minute
                    onTimeSelected(
                            "${selectedHour.toString().padStart(2, '0')}:${selectedMinute.toString().padStart(2, '0')}"
                    )
                }
    }

    val scope = rememberCoroutineScope()

    Box(
            modifier = Modifier.fillMaxWidth().height(itemHeight * visibleItemsCount),
            contentAlignment = Alignment.Center
    ) {
        // Selection Indicator
        Box(
                modifier =
                        Modifier.fillMaxWidth()
                                .height(itemHeight)
                                .alpha(0.1f)
                                .background(MaterialTheme.colorScheme.primary)
        )

        Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
        ) {
            // Hours
            LazyColumn(
                    state = hourListState,
                    flingBehavior = hourFlingBehavior,
                    modifier = Modifier.width(60.dp).fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally
            ) {
                items(Int.MAX_VALUE) { index ->
                    val hour = hours[index % hours.size]
                    val isSelected = hour == selectedHour
                    Box(
                            modifier =
                                    Modifier.height(itemHeight).clickable {
                                        scope.launch {
                                            hourListState.animateScrollToItem(index - 1)
                                        }
                                    },
                            contentAlignment = Alignment.Center
                    ) {
                        Text(
                                text = hour.toString().padStart(2, '0'),
                                style = MaterialTheme.typography.titleLarge,
                                color = if (isSelected) LuklanColors.Primary else Color.Gray,
                                modifier = Modifier.alpha(if (isSelected) 1f else 0.5f)
                        )
                    }
                }
            }

            Text(
                    text = ":",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(horizontal = 8.dp)
            )

            // Minutes
            LazyColumn(
                    state = minuteListState,
                    flingBehavior = minuteFlingBehavior,
                    modifier = Modifier.width(60.dp).fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally
            ) {
                items(Int.MAX_VALUE) { index ->
                    val minute = minutes[index % minutes.size]
                    val isSelected = minute == selectedMinute
                    Box(
                            modifier =
                                    Modifier.height(itemHeight).clickable {
                                        scope.launch {
                                            minuteListState.animateScrollToItem(index - 1)
                                        }
                                    },
                            contentAlignment = Alignment.Center
                    ) {
                        Text(
                                text = minute.toString().padStart(2, '0'),
                                style = MaterialTheme.typography.titleLarge,
                                color =
                                        if (isSelected) MaterialTheme.colorScheme.primary
                                        else Color.Gray,
                                modifier = Modifier.alpha(if (isSelected) 1f else 0.5f)
                        )
                    }
                }
            }
        }
    }
}
