package com.commu.luklan

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import com.commu.luklan.ui.theme.LuklanTheme
import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.commu.luklan.data.Medicine
import com.commu.luklan.data.getNotificationScheduler
import com.commu.luklan.navigation.Screen
import com.commu.luklan.ui.main.MainScreen
import com.commu.luklan.ui.login.LoginScreen
import com.commu.luklan.ui.medicine.AddMedicineScreen
import com.commu.luklan.ui.medicine.EditMedicineScreen
import com.commu.luklan.ui.medicine.MedicineDetailScreen
import com.commu.luklan.ui.onboarding.OnboardingScreen
import com.commu.luklan.ui.signup.SignupScreen
import com.commu.luklan.ui.splash.SplashScreen
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import com.commu.luklan.utils.getCurrentTimeMillis
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@Composable
@Preview
fun App(initialMedicineId: String? = null, initialTime: String? = null) {
    val focusManager = LocalFocusManager.current

    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = LuklanTheme.colors.Primary,
            onPrimary = LuklanTheme.colors.OnPrimary,
            secondary = LuklanTheme.colors.Secondary,
            onSecondary = LuklanTheme.colors.OnSecondary,
            background = LuklanTheme.colors.Background,
            surface = LuklanTheme.colors.Surface,
            surfaceVariant = LuklanTheme.colors.SurfaceVariant,
            error = LuklanTheme.colors.Error,
            onBackground = LuklanTheme.colors.TextPrimary,
            onSurface = LuklanTheme.colors.TextPrimary
        )
    ) {
        val navController = rememberNavController()
        var medicineToEdit by remember { mutableStateOf<Medicine?>(null) }
        var deepLinkTimeForDetail by remember { mutableStateOf<String?>(null) }
        var selectedSignupRole by remember { mutableStateOf("patient") }
        var onboardingInitialPage by remember { mutableStateOf(0) }

        var selectedPatientId by remember { mutableStateOf("") }
        var selectedPatientName by remember { mutableStateOf("") }
        var selectedGroupId by remember { mutableStateOf("") }
        var selectedGroupName by remember { mutableStateOf("") }
        var selectedMainTab by remember { mutableStateOf(com.commu.luklan.ui.main.MainTab.HOME) }

        val authRepository = remember { com.commu.luklan.data.getAuthRepository() }
        val medicineRepository = remember { com.commu.luklan.data.getMedicineRepository() }
        val scope = rememberCoroutineScope()

        LaunchedEffect(initialMedicineId, initialTime) {
            if (initialMedicineId != null && authRepository.isUserLoggedIn()) {
                val userId = authRepository.getCurrentUserId()
                if (userId != null) {
                    medicineRepository.getMedicines(userId).onSuccess { list ->
                        val target = list.find { it.id == initialMedicineId }
                        if (target != null) {
                            medicineToEdit = target
                            deepLinkTimeForDetail = initialTime
                            val now = Instant.fromEpochMilliseconds(getCurrentTimeMillis())
                                .toLocalDateTime(TimeZone.currentSystemDefault())
                            val todayStr = "${now.year}-${
                                now.monthNumber.toString().padStart(2, '0')
                            }-${now.dayOfMonth.toString().padStart(2, '0')}"

                            navController.navigate("${Screen.MedicineDetail.route}/$todayStr")
                        }
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    focusManager.clearFocus()
                }
        ) {
            NavHost(navController = navController, startDestination = Screen.Splash.route) {
                composable(Screen.Splash.route) {
                    SplashScreen(
                        onNavigateToOnboarding = {
                            onboardingInitialPage = 0
                            navController.navigate(Screen.Onboarding.route) {
                                popUpTo(Screen.Splash.route) { inclusive = true }
                            }
                        },
                        onNavigateToHome = {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Splash.route) { inclusive = true }
                            }
                        }
                    )
                }

                composable(Screen.Onboarding.route) {
                    OnboardingScreen(
                        initialPage = onboardingInitialPage,
                        onNavigateToLogin = {
                            navController.navigate(Screen.Login.route)
                        },
                        onNavigateToSignup = { role ->
                            selectedSignupRole = role
                            navController.navigate(Screen.Signup.route)
                        }
                    )
                }

                composable(Screen.Login.route) {
                    LoginScreen(
                        onNavigateToHome = {
                            scope.launch {
                                val userId = authRepository.getCurrentUserId()
                                if (userId != null) {
                                    // Sync notifications on login
                                    medicineRepository.getMedicines(userId).onSuccess { meds ->
                                        meds.forEach { getNotificationScheduler().schedule(it) }
                                    }
                                }
                                navController.navigate(Screen.Home.route) {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        },
                        onNavigateToSignup = {
                            onboardingInitialPage = 3 // Last page
                            navController.navigate(Screen.Onboarding.route) {
                                popUpTo(Screen.Onboarding.route) { inclusive = true }
                            }
                        }
                    )
                }

                composable(Screen.Signup.route) {
                    SignupScreen(
                        role = selectedSignupRole,
                        onNavigateToHome = {
                            if (selectedSignupRole == "caretaker") {
                                navController.navigate(Screen.JoinGroup.route) {
                                    popUpTo(0) { inclusive = true }
                                }
                            } else {
                                navController.navigate(Screen.InviteCaretaker.route) {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        },
                        onNavigateToLogin = {
                            navController.navigate(Screen.Login.route) {
                                popUpTo(Screen.Signup.route) { inclusive = true }
                            }
                        }
                    )
                }

                composable(Screen.Home.route) {
                    LaunchedEffect(Unit) { selectedPatientId = "" }
                    MainScreen(
                        selectedTab = selectedMainTab,
                        onTabSelected = { selectedMainTab = it },
                        onNavigateToAddMedicine = {
                            selectedPatientId = ""
                            navController.navigate(Screen.AddMedicine.route)
                        },
                        onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
                        onNavigateToMedicineDetail = { med, date ->
                            medicineToEdit = med
                            navController.navigate("${Screen.MedicineDetail.route}/$date")
                        },
                        onNavigateToHistory = { navController.navigate(Screen.History.route) },
                        onNavigateToMedicineGroups = { navController.navigate(Screen.MedicineGroups.route) },
                        onNavigateToInviteCaretaker = { navController.navigate(Screen.InviteCaretaker.route) },
                        onNavigateToCaretakerDashboard = { navController.navigate(Screen.CaretakerDashboard.route) },
                        onNavigateToPatientTimeline = { id, name ->
                            selectedPatientId = id
                            selectedPatientName = name
                            navController.navigate(Screen.PatientTimeline.route)
                        }
                    )
                }

                composable(Screen.PatientTimeline.route) {
                    com.commu.luklan.ui.home.HomeScreen(
                        targetUserId = selectedPatientId,
                        targetUserName = selectedPatientName,
                        onBack = { navController.popBackStack() },
                        onNavigateToAddMedicine = { navController.navigate(Screen.AddMedicine.route) },
                        onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
                        onNavigateToMedicineDetail = { medicine, date ->
                            medicineToEdit = medicine
                            navController.navigate("${Screen.MedicineDetail.route}/$date")
                        }
                    )
                }

                composable(Screen.InviteCaretaker.route) {
                    com.commu.luklan.ui.caretaker.InviteCaretakerScreen(
                        onBack = { 
                            if (!navController.popBackStack()) {
                                navController.navigate(Screen.Home.route) {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        }
                    )
                }

                composable(Screen.CaretakerDashboard.route) {
                    com.commu.luklan.ui.caretaker.CaretakerDashboardScreen(
                        onBack = { 
                            if (!navController.popBackStack()) {
                                navController.navigate(Screen.Home.route) {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        },
                        onNavigateToJoin = { navController.navigate(Screen.JoinGroup.route) },
                        onNavigateToMembers = { id, name ->
                            selectedGroupId = id
                            selectedGroupName = name
                            navController.navigate(Screen.GroupMembers.route)
                        }
                    )
                }

                composable(Screen.GroupMembers.route) {
                    com.commu.luklan.ui.caretaker.GroupMembersScreen(
                        groupId = selectedGroupId,
                        groupName = selectedGroupName,
                        onBack = { 
                            if (!navController.popBackStack()) {
                                navController.navigate(Screen.Home.route) {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        },
                        onNavigateToInvite = { navController.navigate(Screen.InviteCaretaker.route) },
                        onNavigateToPatientTimeline = { id, name ->
                            selectedPatientId = id
                            selectedPatientName = name
                            navController.navigate(Screen.PatientTimeline.route)
                        }
                    )
                }

                composable(Screen.JoinGroup.route) {
                    com.commu.luklan.ui.caretaker.JoinCaretakerScreen(
                        onBack = { 
                            if (!navController.popBackStack()) {
                                navController.navigate(Screen.Home.route) {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        },
                        onSuccess = {
                            navController.navigate(Screen.CaretakerDashboard.route) {
                                popUpTo(Screen.JoinGroup.route) { inclusive = true }
                            }
                        }
                    )
                }

                composable(Screen.QRScanner.route) {
                    com.commu.luklan.ui.caretaker.JoinCaretakerScreen(
                        onBack = { navController.popBackStack() },
                        onSuccess = {
                            navController.navigate(Screen.CaretakerDashboard.route) {
                                popUpTo(Screen.QRScanner.route) { inclusive = true }
                            }
                        }
                    )
                }

                composable(Screen.AddMedicine.route) {
                    AddMedicineScreen(
                        targetUserId = selectedPatientId.takeIf { it.isNotEmpty() },
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable("${Screen.MedicineDetail.route}/{selectedDate}") { backStackEntry ->
                    val date = backStackEntry.savedStateHandle.get<String>("selectedDate")
                    medicineToEdit?.let { medicine ->
                        MedicineDetailScreen(
                            medicine = medicine,
                            initialSlotTime = deepLinkTimeForDetail,
                            selectedDate = date,
                            onBack = {
                                navController.popBackStack()
                            },
                            onEdit = {
                                navController.navigate(Screen.EditMedicine.route)
                            },
                            onMedicineTaken = {
                                navController.popBackStack()
                            }
                        )
                        SideEffect {
                            deepLinkTimeForDetail = null
                        }
                    }
                }

                composable(Screen.EditMedicine.route) {
                    medicineToEdit?.let { medicine ->
                        EditMedicineScreen(
                            medicine = medicine,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                }

                composable(Screen.History.route) {
                    com.commu.luklan.ui.history.HistoryScreen(
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(Screen.MedicineGroups.route) {
                    com.commu.luklan.ui.groups.MedicineGroupsScreen(
                        onBack = { navController.popBackStack() },
                        onMedicineClick = { medicine ->
                            medicineToEdit = medicine
                            val now = Instant.fromEpochMilliseconds(getCurrentTimeMillis())
                                .toLocalDateTime(TimeZone.currentSystemDefault())
                            val todayStr = "${now.year}-${
                                now.monthNumber.toString().padStart(2, '0')
                            }-${now.dayOfMonth.toString().padStart(2, '0')}"


                            navController.navigate("${Screen.MedicineDetail.route}/$todayStr")
                        }
                    )
                }

                composable(Screen.Profile.route) {
                    com.commu.luklan.ui.profile.ProfileScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onLogoutSuccess = {
                            navController.navigate(Screen.Login.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    )
                }
            }
        }
    }
}
