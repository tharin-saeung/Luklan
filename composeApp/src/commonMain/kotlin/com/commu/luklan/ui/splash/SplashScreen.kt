package com.commu.luklan.ui.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.commu.luklan.data.AuthRepository
import com.commu.luklan.ui.theme.LuklanTheme.LuklanTypography
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onNavigateToOnboarding: () -> Unit, onNavigateToHome: () -> Unit) {
    val authRepository = remember { AuthRepository() }

    // Check if user is logged in
    LaunchedEffect(Unit) {
        delay(2000) // Show splash for 2 seconds
        if (authRepository.isUserLoggedIn()) {
            onNavigateToHome()
        } else {
            onNavigateToOnboarding()
        }
    }

    Box(
            modifier =
                    Modifier.fillMaxSize()
                            .background(Color(0xFF87CEEB)), // Light blue like in your design
            contentAlignment = Alignment.Center
    ) {
        Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
        ) {
            Text(
                    text = "ลูกหลาน",
                    style = LuklanTypography.h1,
                    fontSize = 48.sp,
                    color = Color.White,
                    textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                    text = "Luklan",
                    style = LuklanTypography.h1,
                    fontSize = 36.sp,
                    color = Color.White,
                    textAlign = TextAlign.Center
            )
        }
    }
}
