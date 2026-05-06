package com.commu.luklan.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.commu.luklan.ui.theme.LuklanTheme
import com.commu.luklan.ui.theme.LuklanTheme.LuklanTypography
import com.commu.luklan.ui.theme.LuklanTheme.LuklanColors
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(
    initialPage: Int = 0,
    onNavigateToLogin: () -> Unit,
    onNavigateToSignup: (role: String) -> Unit
) {
    val pages = listOf(
        OnboardingData(
            "จัดการเรื่องยาให้เป็นเรื่องง่าย",
            "บันทึกและติดตามการใช้ยาของคุณและคนที่คุณรักได้อย่างแม่นยำ",
            "💊"
        ),
        OnboardingData(
            "แจ้งเตือนไม่ให้ลืม",
            "ระบบแจ้งเตือนที่ออกแบบมาเพื่อทุกคน ชัดเจนและเข้าใจง่าย",
            "⏰"
        ),
        OnboardingData(
            "ดูแลกันได้จากทุกที่",
            "เชื่อมต่อข้อมูลระหว่างผู้ป่วยและผู้ดูแล เพื่อความอุ่นใจของคนในครอบครัว",
            "👨‍👩‍👧‍👦"
        ),
        OnboardingData(
            "ยินดีต้อนรับสู่แอปพลิเคชัน\nลูกหลาน",
            "ดูแลท่านเสมือนกับลูกหลาน\nที่คอยอยู่เคียงข้าง",
            "🎉"
        )
    )

    val pagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = { pages.size }
    )
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LuklanColors.Background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { page ->
                OnboardingPage(data = pages[page], isLastPage = page == pages.size - 1) {
                    // Role selection for last page
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Button(
                            onClick = { onNavigateToSignup("patient") },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(28.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = LuklanColors.Primary)
                        ) {
                            Text("ฉันเป็นผู้ป่วย", style = LuklanTypography.h3, color = Color.White)
                        }
                        
                        Button(
                            onClick = { onNavigateToSignup("caretaker") },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(28.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = LuklanColors.Secondary)
                        ) {
                            Text("ฉันเป็นผู้ดูแล", style = LuklanTypography.h3, color = Color.White)
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "มีบัญชีอยู่แล้ว?",
                                style = LuklanTypography.bodyMedium,
                                color = LuklanColors.TextSecondary
                            )
                            TextButton(
                                onClick = onNavigateToLogin,
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                            ) {
                                Text(
                                    text = "เข้าสู่ระบบ",
                                    style = LuklanTypography.bodyMedium,
                                    color = LuklanColors.Primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Bottom controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Page Indicator
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    repeat(pages.size) { iteration ->
                        val color = if (pagerState.currentPage == iteration) 
                            LuklanColors.Secondary else Color.LightGray
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(color)
                        )
                    }
                }

                // Next/Get Started Button
                if (pagerState.currentPage < pages.size - 1) {
                    TextButton(
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        }
                    ) {
                        Text(
                            text = "ถัดไป",
                            color = LuklanColors.Primary,
                            style = LuklanTypography.h3,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

data class OnboardingData(
    val title: String,
    val description: String,
    val emoji: String
)

@Composable
fun OnboardingPage(
    data: OnboardingData,
    isLastPage: Boolean,
    roleSelection: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(200.dp)
                .clip(RoundedCornerShape(32.dp))
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Text(data.emoji, fontSize = 80.sp)
        }

        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = data.title,
            style = LuklanTypography.h2,
            textAlign = TextAlign.Center,
            color = LuklanColors.TextPrimary,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = data.description,
            style = LuklanTypography.bodyLarge,
            textAlign = TextAlign.Center,
            color = LuklanColors.TextSecondary
        )

        if (isLastPage) {
            Spacer(modifier = Modifier.height(48.dp))
            roleSelection()
        }
    }
}
