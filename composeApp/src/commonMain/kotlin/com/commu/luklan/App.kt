package com.commu.luklan

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.commu.luklan.data.*
import com.commu.luklan.navigation.Screen
import com.commu.luklan.ui.caretaker.*
import com.commu.luklan.ui.contact.ContactScreen
import com.commu.luklan.ui.groups.MedicineGroupsScreen
import com.commu.luklan.ui.history.HistoryScreen
import com.commu.luklan.ui.history.NotificationCenterScreen
import com.commu.luklan.ui.home.HomeScreen
import com.commu.luklan.ui.login.LoginScreen
import com.commu.luklan.ui.main.MainScreen
import com.commu.luklan.ui.main.MainTab
import com.commu.luklan.ui.medicine.AddMedicineScreen
import com.commu.luklan.ui.medicine.EditMedicineScreen
import com.commu.luklan.ui.medicine.MedicineDetailScreen
import com.commu.luklan.ui.ocr.AddMethodScreen
import com.commu.luklan.ui.ocr.OcrScanScreen
import com.commu.luklan.ui.onboarding.OnboardingScreen
import com.commu.luklan.ui.profile.ProfileScreen
import com.commu.luklan.ui.signup.SignupScreen
import com.commu.luklan.ui.splash.SplashScreen
import com.commu.luklan.ui.theme.LuklanColors
import com.commu.luklan.ui.theme.LuklanTheme
import com.commu.luklan.ui.theme.LuklanTypography
import com.commu.luklan.utils.getCurrentTimeMillis
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@Composable
@Preview
fun App(deepLinkMedicineId: String? = null, deepLinkTime: String? = null) {
    MaterialTheme {
        val navController = rememberNavController()
        val authRepository = remember { getAuthRepository() }
        val scope = rememberCoroutineScope()
        
        var medicineToEdit by remember { mutableStateOf<Medicine?>(null) }
        var selectedPatientId by remember { mutableStateOf<String?>(null) }
        var selectedPatientName by remember { mutableStateOf<String?>(null) }
        var selectedTargetUserIdForNotif by remember { mutableStateOf<String?>(null) }
        var selectedGroupId by remember { mutableStateOf("") }
        var selectedGroupName by remember { mutableStateOf("") }
        
        var currentTab by remember { mutableStateOf(MainTab.HOME) }

        LaunchedEffect(deepLinkMedicineId, deepLinkTime) {
            if (deepLinkMedicineId != null && deepLinkTime != null) {
                navController.navigate("${Screen.MedicineDetail.route}/$deepLinkTime")
            }
        }

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = LuklanColors.Background
        ) {
            NavHost(
                navController = navController,
                startDestination = Screen.Splash.route
            ) {
                composable(Screen.Splash.route) {
                    SplashScreen(
                        onNavigateToHome = {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Splash.route) { inclusive = true }
                            }
                        },
                        onNavigateToOnboarding = {
                            navController.navigate(Screen.Onboarding.route) {
                                popUpTo(Screen.Splash.route) { inclusive = true }
                            }
                        }
                    )
                }

                composable(Screen.Onboarding.route) {
                    OnboardingScreen(
                        onNavigateToLogin = { navController.navigate(Screen.Login.route) },
                        onNavigateToSignup = { navController.navigate(Screen.Signup.route) }
                    )
                }

                composable(Screen.Login.route) {
                    LoginScreen(
                        onNavigateToHome = {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        },
                        onNavigateToSignup = { navController.navigate(Screen.Signup.route) }
                    )
                }

                composable(Screen.Signup.route) {
                    SignupScreen(
                        onNavigateToHome = {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Signup.route) { inclusive = true }
                            }
                        },
                        onNavigateToLogin = { navController.popBackStack() }
                    )
                }

                composable(Screen.Home.route) {
                    MainScreen(
                        selectedTab = currentTab,
                        onTabSelected = { currentTab = it },
                        onNavigateToAddMedicine = { navController.navigate(Screen.AddMethod.route) },
                        onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
                        onLogout = {
                            // Auth handled inside Profile usually or here
                        },
                        onNavigateToMedicineDetail = { med, date ->
                            medicineToEdit = med
                            navController.navigate("${Screen.MedicineDetail.route}/$date")
                        },
                        onNavigateToHistory = { uid ->
                            selectedTargetUserIdForNotif = uid
                            navController.navigate(Screen.History.route)
                        },
                        onNavigateToMedicineGroups = { uid ->
                            selectedTargetUserIdForNotif = uid
                            navController.navigate(Screen.MedicineGroups.route)
                        },
                        onNavigateToInviteCaretaker = { navController.navigate(Screen.InviteCaretaker.route) },
                        onNavigateToInviteCaretakerWithId = { gid ->
                            navController.navigate("${Screen.InviteCaretaker.route}?groupId=$gid")
                        },
                        onNavigateToCaretakerDashboard = { navController.navigate(Screen.CaretakerDashboard.route) },
                        onNavigateToPatientTimeline = { id, name ->
                            selectedPatientId = id
                            selectedPatientName = name
                            navController.navigate(Screen.PatientTimeline.route)
                        },
                        onNavigateToNotificationCenter = { targetUid: String -> 
                            selectedTargetUserIdForNotif = targetUid
                            navController.navigate(Screen.NotificationCenter.route) 
                        },
                        onMedicineClick = { medicine ->
                            medicineToEdit = medicine
                            val now = Instant.fromEpochMilliseconds(getCurrentTimeMillis()).toLocalDateTime(TimeZone.currentSystemDefault())
                            val todayStr = "${now.year}-${now.monthNumber.toString().padStart(2, '0')}-${now.dayOfMonth.toString().padStart(2, '0')}"
                            navController.navigate("${Screen.MedicineDetail.route}/$todayStr")
                        }
                    )
                }

                composable(Screen.PatientTimeline.route) {
                    HomeScreen(
                        targetUserId = selectedPatientId,
                        targetUserName = selectedPatientName,
                        onBack = { 
                            selectedPatientId = null
                            selectedPatientName = null
                            navController.popBackStack() 
                        },
                        onNavigateToAddMedicine = { navController.navigate(Screen.AddMethod.route) },
                        onNavigateToProfile = {
                            navController.navigate(Screen.Profile.route)
                        },
                        onNavigateToMedicineDetail = { medicine, date ->
                            medicineToEdit = medicine
                            navController.navigate("${Screen.MedicineDetail.route}/$date")
                        },
                        onNavigateToHistory = { uid ->
                            selectedTargetUserIdForNotif = uid ?: selectedPatientId
                            navController.navigate(Screen.History.route)
                        },
                        onNavigateToMedicineGroups = { uid ->
                            selectedTargetUserIdForNotif = uid ?: selectedPatientId
                            navController.navigate(Screen.MedicineGroups.route)
                        },
                        onNavigateToNotificationCenter = { targetUid: String -> 
                            selectedTargetUserIdForNotif = targetUid
                            navController.navigate(Screen.NotificationCenter.route) 
                        }
                    )
                }

                composable(Screen.Profile.route) {
                    ProfileScreen(
                        onNavigateBack = { 
                            if (!navController.popBackStack()) {
                                navController.navigate(Screen.Home.route) {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        },
                        onNavigateToGroups = { navController.navigate(Screen.CaretakerDashboard.route) },
                        onNavigateToHistory = { 
                            selectedTargetUserIdForNotif = null
                            navController.navigate(Screen.History.route) 
                        },
                        onNavigateToContact = { navController.navigate(Screen.Contact.route) },
                        onLogoutSuccess = {
                            navController.navigate(Screen.Login.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    )
                }

                composable(Screen.Contact.route) {
                    ContactScreen(onBack = { navController.popBackStack() })
                }

                composable(
                    route = "${Screen.InviteCaretaker.route}?groupId={groupId}",
                    arguments = listOf(navArgument("groupId") { type = NavType.StringType; nullable = true; defaultValue = null })
                ) { backStackEntry ->
                    val gid = backStackEntry.arguments?.getString("groupId")
                    InviteCaretakerScreen(
                        groupId = gid,
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
                    CaretakerDashboardScreen(
                        onBack = { 
                            if (!navController.popBackStack()) {
                                navController.navigate(Screen.Home.route) {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        },
                        onNavigateToJoin = { navController.navigate(Screen.JoinGroup.route) },
                        onNavigateToCreate = { navController.navigate(Screen.CreateGroup.route) },
                        onNavigateToMembers = { id, name ->
                            selectedGroupId = id
                            selectedGroupName = name
                            navController.navigate(Screen.GroupMembers.route)
                        }
                    )
                }

                composable(Screen.GroupMembers.route) {
                    GroupMembersScreen(
                        groupId = selectedGroupId,
                        groupName = selectedGroupName,
                        onBack = { navController.popBackStack() },
                        onNavigateToInvite = { gid ->
                            navController.navigate("${Screen.InviteCaretaker.route}?groupId=$gid")
                        },
                        onNavigateToPatientTimeline = { id, name ->
                            selectedPatientId = id
                            selectedPatientName = name
                            navController.navigate(Screen.PatientTimeline.route)
                        }
                    )
                }

                composable(Screen.JoinGroup.route) {
                    JoinCaretakerScreen(
                        onBack = { navController.popBackStack() },
                        onSuccess = { 
                            navController.navigate(Screen.CaretakerDashboard.route) {
                                popUpTo(Screen.JoinGroup.route) { inclusive = true }
                            }
                        }
                    )
                }

                composable(Screen.CreateGroup.route) {
                    CreateGroupScreen(
                        onBack = { navController.popBackStack() },
                        onSuccess = {
                            navController.navigate(Screen.CaretakerDashboard.route) {
                                popUpTo(Screen.CreateGroup.route) { inclusive = true }
                            }
                        }
                    )
                }

                composable(Screen.AddMedicine.route) {
                    AddMedicineScreen(
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable(
                    route = "${Screen.MedicineDetail.route}/{date}",
                    arguments = listOf(navArgument("date") { type = NavType.StringType })
                ) { backStackEntry ->
                    val date = backStackEntry.arguments?.getString("date") ?: ""
                    medicineToEdit?.let { medicine ->
                        MedicineDetailScreen(
                            medicine = medicine,
                            selectedDate = date,
                            onBack = { navController.popBackStack() },
                            onEdit = { navController.navigate(Screen.EditMedicine.route) },
                            onMedicineTaken = { }
                        )
                    }
                }

                composable(Screen.EditMedicine.route) {
                    medicineToEdit?.let { medicine ->
                        EditMedicineScreen(
                            medicine = medicine,
                            onNavigateBack = { updated ->
                                medicineToEdit = updated ?: medicine
                                navController.popBackStack()
                            }
                        )
                    }
                }

                composable(Screen.History.route) {
                    HistoryScreen(
                        targetUserId = selectedTargetUserIdForNotif,
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(Screen.MedicineGroups.route) {
                    MedicineGroupsScreen(
                        targetUserId = selectedTargetUserIdForNotif,
                        onBack = { navController.popBackStack() },
                        onMedicineClick = { medicine ->
                            medicineToEdit = medicine
                            val now = Instant.fromEpochMilliseconds(getCurrentTimeMillis()).toLocalDateTime(TimeZone.currentSystemDefault())
                            val todayStr = "${now.year}-${now.monthNumber.toString().padStart(2, '0')}-${now.dayOfMonth.toString().padStart(2, '0')}"
                            navController.navigate("${Screen.MedicineDetail.route}/$todayStr")
                        }
                    )
                }

                composable(Screen.NotificationCenter.route) {
                    NotificationCenterScreen(
                        targetUserId = selectedTargetUserIdForNotif,
                        onBack = { navController.popBackStack() },
                        onMedicineClick = { medicine ->
                            medicineToEdit = medicine
                            val now = Instant.fromEpochMilliseconds(getCurrentTimeMillis()).toLocalDateTime(TimeZone.currentSystemDefault())
                            val todayStr = "${now.year}-${now.monthNumber.toString().padStart(2, '0')}-${now.dayOfMonth.toString().padStart(2, '0')}"
                            navController.navigate("${Screen.MedicineDetail.route}/$todayStr")
                        }
                    )
                }

                composable(Screen.OcrScan.route) {
                    OcrScanScreen(
                        onBack = { navController.popBackStack() },
                        onProceedToAdd = { navController.navigate(Screen.AddMethod.route) }
                    )
                }

                composable(Screen.AddMethod.route) {
                    AddMethodScreen(
                        onManual = {
                            navController.navigate(Screen.AddMedicine.route) {
                                popUpTo(Screen.AddMethod.route) { inclusive = true }
                            }
                        },
                        onOcr = {
                            navController.navigate(Screen.OcrScan.route) {
                                popUpTo(Screen.AddMethod.route) { inclusive = true }
                            }
                        },
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}

