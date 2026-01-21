package com.commu.luklan.ui.onboarding

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.commu.luklan.ui.theme.LuklanTheme
import com.commu.luklan.ui.theme.LuklanTypography
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(onNavigateToLogin: () -> Unit, onNavigateToSignup: () -> Unit) {
    val onboardingPages = remember {
        listOf(
                OnboardingPage(
                        title = "แจ้งเตือนการกินยาได้\nอย่างแม่นยำ",
                        description = "ติดตามการกินยาของคุณอย่างสม่ำเสมอ",
                        imageDescription = "💊"
                ),
                OnboardingPage(
                        title = "สามารถจัดกลุ่มยาได้\nตามต้องการ",
                        description = "จัดกลุ่มยาตามประเภทหรือวิธีการใช้งาน",
                        imageDescription = "📋"
                ),
                OnboardingPage(
                        title = "สามารถอ่านรายละเอียดเกี่ยวกับยาเบื้องต้น",
                        description = "อ่านรายละเอียดเกี่ยวกับยาของคุณเพิ่มเติม",
                        imageDescription = "📖"
                ),
                OnboardingPage(
                        title = "สามารถส่งสัญญาณ SOS ได้เมื่อมีเหตุฉุกเฉิน",
                        description = "แจ้งเตือนผู้ดูแลเมื่อมีเหตุฉุกเฉิน",
                        imageDescription = "🚨"
                ),
                OnboardingPage(
                        title = "ยินดีต้อนรับสู่แอปพลิเคชัน\nลูกหลาน",
                        description = "ดูแลท่านเสมือนกับลูกหลาน\nที่คอยอยู่เคียงข้าง",
                        imageDescription = "🎉",
                        isLastPage = true
                )
        )
    }

    val pagerState = rememberPagerState(pageCount = { onboardingPages.size })
    val scope = rememberCoroutineScope()

    Column(
            modifier =
                    Modifier.fillMaxSize()
                            .background(LuklanTheme.colors.Background)
                            .statusBarsPadding() // Only status bar padding
    ) {
        HorizontalPager(state = pagerState, modifier = Modifier.weight(1f)) { page ->
            if (page < onboardingPages.size) {
                OnboardingPageContent(
                        page = onboardingPages[page],
                        modifier = Modifier.fillMaxSize()
                )
            }
        }

        Column(
                modifier =
                        Modifier.fillMaxWidth()
                                .padding(horizontal = LuklanTheme.spacing.xl)
                                .padding(
                                        bottom = LuklanTheme.spacing.xl
                                ) // Consistent padding for both platforms
                                .navigationBarsPadding(), // Only navigation bar padding
                horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Page indicators
            Row(horizontalArrangement = Arrangement.spacedBy(LuklanTheme.spacing.sm)) {
                repeat(onboardingPages.size) { index ->
                    Box(
                            modifier =
                                    Modifier.size(LuklanTheme.dimensions.indicatorSize)
                                            .clip(CircleShape)
                                            .background(
                                                    if (index == pagerState.currentPage)
                                                            LuklanTheme.colors.IndicatorActive
                                                    else LuklanTheme.colors.Indicator
                                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(LuklanTheme.spacing.xl))

            // Action button
            if (pagerState.currentPage == onboardingPages.size - 1) {
                // Last page - show "เริ่มต้น" button
                Button(
                        onClick = onNavigateToSignup,
                        modifier =
                                Modifier.fillMaxWidth().height(LuklanTheme.dimensions.buttonLarge),
                        shape = RoundedCornerShape(LuklanTheme.dimensions.radiusLarge),
                        colors =
                                ButtonDefaults.buttonColors(
                                        containerColor = LuklanTheme.colors.Primary
                                )
                ) {
                    Text(
                            text = "สมัครสมาชิกเลย",
                            style = LuklanTypography.bodyLarge,
                            color = LuklanTheme.colors.OnPrimary
                    )
                }

                Spacer(modifier = Modifier.height(LuklanTheme.spacing.sm))

                TextButton(onClick = onNavigateToLogin) {
                    Text(
                            text = "เข้าสู่ระบบ",
                            fontSize = LuklanTypography.bodyMedium.fontSize,
                            color = LuklanTheme.colors.Primary
                    )
                }
            } else {
                // Other pages - show "ต่อไป" button
                Button(
                        onClick = {
                            scope.launch {
                                if (pagerState.currentPage < onboardingPages.size - 1) {
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                }
                            }
                        },
                        modifier = Modifier.size(LuklanTheme.dimensions.buttonCircle),
                        shape = CircleShape,
                        colors =
                                ButtonDefaults.buttonColors(
                                        containerColor = LuklanTheme.colors.Primary
                                ),
                        contentPadding = PaddingValues(0.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                                text = "→",
                                style = LuklanTheme.typography.navigationIcon,
                                color = LuklanTheme.colors.OnPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OnboardingPageContent(page: OnboardingPage, modifier: Modifier = Modifier) {
    Column(
            modifier = modifier.padding(LuklanTheme.spacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
    ) {
        // Image container
        Box(
                modifier =
                        Modifier.size(LuklanTheme.dimensions.imageContainer)
                                .clip(RoundedCornerShape(LuklanTheme.dimensions.radiusMedium))
                                .background(LuklanTheme.colors.Surface),
                contentAlignment = Alignment.Center
        ) {
            Text(
                    text = page.imageDescription,
                    fontSize = LuklanTypography.h1.fontSize,
                    textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(LuklanTheme.spacing.xxl))

        Text(
                text = page.title,
                style = LuklanTheme.typography.h3,
                textAlign = TextAlign.Center,
                color = LuklanTheme.colors.TextPrimary
        )

        Spacer(modifier = Modifier.height(LuklanTheme.spacing.md))

        Text(
                text = page.description,
                style = LuklanTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = LuklanTheme.colors.TextSecondary
        )
    }
}

data class OnboardingPage(
        val title: String,
        val description: String,
        val imageDescription: String,
        val isLastPage: Boolean = false
)
