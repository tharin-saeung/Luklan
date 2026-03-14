package com.commu.luklan.ui.components

import androidx.compose.runtime.Composable

@Composable
expect fun DatePickerDialog(
    initialDate: String = "",
    onDateSelected: (String) -> Unit,
    onDismiss: () -> Unit
)
