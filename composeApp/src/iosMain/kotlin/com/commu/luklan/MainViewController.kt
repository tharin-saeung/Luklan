package com.commu.luklan

import androidx.compose.ui.window.ComposeUIViewController

fun MainViewController() = ComposeUIViewController { 
    App(
        initialMedicineId = DeepLinkManager.medicineId,
        initialTime = DeepLinkManager.time
    ) 
}
