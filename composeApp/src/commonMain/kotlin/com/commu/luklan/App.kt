package com.commu.luklan

import androidx.compose.animation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.savedstate.read
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
import com.commu.luklan.ui.onboarding.OnboardingScreen
import com.commu.luklan.ui.profile.ProfileScreen
import com.commu.luklan.ui.signup.SignupScreen
import com.commu.luklan.ui.splash.SplashScreen
import com.commu.luklan.ui.theme.LuklanColors
import com.commu.luklan.ui.theme.LuklanTheme
import com.commu.luklan.utils.getCurrentTimeMillis
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@Composable
@Preview
fun App(deepLinkMedicineId: String? = null, deepLinkTime: String? = null) {
    LuklanTheme {
        val navController = rememberNavController()
        val authRepository = remember { getAuthRepository() }
        val focusManager = LocalFocusManager.current
        val scope = rememberCoroutineScope()
        
        var medicineToEdit by remember { mutableStateOf<Medicine?>(null) }
        var selectedPatientId by remember { mutableStateOf<String?>(null) }
        var selectedPatientName by remember { mutableStateOf<String?>(null) }
        var selectedTargetUserIdForNotif by remember { mutableStateOf<String?>(null) }
        var selectedGroupId by remember { mutableStateOf("") }
        var selectedGroupName by remember { mutableStateOf("") }
        
        var currentTab by remember { mutableStateOf(MainTab.HOME) }
        var activeDeepLinkTime by remember { mutableStateOf<String?>(null) }

        val safeBack: () -> Unit = {
            if (navController.previousBackStackEntry != null) {
                navController.popBackStack()
            } else {
                navController.navigate(Screen.Home.route) {
                    popUpTo(0) { inclusive = true }
                }
            }
        }

        LaunchedEffect(deepLinkMedicineId, deepLinkTime) {
            val uid = authRepository.getCurrentUserId()
            if (deepLinkMedicineId != null && deepLinkTime != null && uid != null) {
                val medicineRepository = getMedicineRepository()
                val result = medicineRepository.observeMedicines(uid).firstOrNull()
                val med = result?.getOrNull()?.find { it.id == deepLinkMedicineId }
                
                if (med != null) {
                    medicineToEdit = med
                    activeDeepLinkTime = deepLinkTime
                    val now = Instant.fromEpochMilliseconds(getCurrentTimeMillis()).toLocalDateTime(TimeZone.currentSystemDefault())
                    val todayStr = "${now.year}-${now.monthNumber.toString().padStart(2, '0')}-${now.dayOfMonth.toString().padStart(2, '0')}"
                    navController.navigate("${Screen.MedicineDetail.route}/$todayStr")
                }
            }
        }

        Surface(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(onTap = {
                        focusManager.clearFocus()
                    })
                },
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
                                popUpTo(0) { inclusive = true }
                            }
                        },
                        onNavigateToOnboarding = {
                            navController.navigate("${Screen.Onboarding.route}?page=0") {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    )
                }

                composable(
                    route = "${Screen.Onboarding.route}?page={page}",
                    arguments = listOf(navArgument("page") { type = NavType.IntType; defaultValue = 0 })
                ) { backStackEntry ->
                    val page = backStackEntry.arguments?.read { getInt("page") } ?: 0
                    OnboardingScreen(
                        initialPage = page,
                        onNavigateToLogin = { 
                            navController.navigate(Screen.Login.route) {
                                popUpTo(Screen.Onboarding.route) { inclusive = true }
                            }
                        },
                        onNavigateToSignup = { role -> navController.navigate("${Screen.Signup.route}?role=$role") }
                    )
                }

                composable(Screen.Login.route) {
                    LoginScreen(
                        onNavigateToHome = {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        },
                        onNavigateToSignup = { navController.navigate("${Screen.Onboarding.route}?page=3") }
                    )
                }

                composable(
                    route = "${Screen.Signup.route}?role={role}",
                    arguments = listOf(navArgument("role") { type = NavType.StringType; defaultValue = "user" })
                ) { backStackEntry ->
                    val role = backStackEntry.arguments?.read { getString("role")} ?: "user"

                    SignupScreen(
                        role = role,
                        onNavigateToHome = {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        },
                        onNavigateToLogin = { 
                            navController.navigate(Screen.Login.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        },
                        onNavigateToInviteCaretaker = { groupId ->
                            navController.navigate("${Screen.InviteCaretaker.route}?groupId=$groupId") {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    )
                }

                composable(Screen.Home.route) {
                    MainScreen(
                        selectedTab = currentTab,
                        onTabSelected = { currentTab = it },
                        onNavigateToAddMedicine = { 
                            selectedPatientId = null
                            navController.navigate(Screen.AddMedicine.route) 
                        },
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

                composable(
                    route = "${Screen.MedSummary.route}?userId={userId}&userName={userName}",
                    arguments = listOf(
                        navArgument("userId") { type = NavType.StringType; nullable = true; defaultValue = null },
                        navArgument("userName") { type = NavType.StringType; nullable = true; defaultValue = null }
                    )
                ) { backStackEntry ->
                    val uid = backStackEntry.arguments?.read { getString("userId") } ?: authRepository.getCurrentUserId() ?: ""
                    val name = backStackEntry.arguments?.read { getString("userName") }
                    com.commu.luklan.ui.summary.MedSummaryScreen(
                        userId = uid,
                        userName = name,
                        onNavigateToStats = { month, year ->
                            navController.navigate("${Screen.MedicineStats.route}?userId=$uid&month=$month&year=$year")
                        },
                        onBack = { safeBack() }
                    )
                }

                composable(
                    route = "${Screen.MedicineStats.route}?userId={userId}&month={month}&year={year}",
                    arguments = listOf(
                        navArgument("userId") { type = NavType.StringType; defaultValue = "" },
                        navArgument("month") { type = NavType.IntType; defaultValue = 1 },
                        navArgument("year") { type = NavType.IntType; defaultValue = 2024 }
                    )
                ) { backStackEntry ->
                    val uid = backStackEntry.arguments?.read { getString("userId") } ?: ""
                    val month = backStackEntry.arguments?.read { getInt("month") } ?: 1
                    val year = backStackEntry.arguments?.read { getInt("year") } ?: 2024
                    com.commu.luklan.ui.summary.MedicineStatsScreen(
                        userId = uid,
                        initialMonth = month,
                        initialYear = year,
                        onBack = { safeBack() }
                    )
                }

                composable(Screen.PatientTimeline.route) {
                    HomeScreen(
                        targetUserId = selectedPatientId,
                        targetUserName = selectedPatientName,
                        onBack = { 
                            navController.popBackStack()
                        },
                        onRefresh = {
                            // If current user is caretaker, sync watchdogs for this patient
                            scope.launch {
                                authRepository.getCurrentUserId()?.let { uid ->
                                    authRepository.getUserProfile(uid).onSuccess { profile ->
                                        if (profile.role == "caretaker") {
                                            val medicineRepository = getMedicineRepository()
                                            val notificationScheduler = getNotificationScheduler()
                                            selectedPatientId?.let { pid ->
                                                medicineRepository.observeMedicines(pid).collectLatest { result ->
                                                    result.onSuccess { medicines ->
                                                        medicines.forEach { med ->
                                                            notificationScheduler.schedule(med)
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        },
                        onNavigateToAddMedicine = { navController.navigate(Screen.AddMedicine.route) },
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

                composable(
                    route = Screen.Profile.route,
                    enterTransition = { slideInHorizontally(initialOffsetX = { -it }) + fadeIn() },
                    exitTransition = { slideOutHorizontally(targetOffsetX = { -it }) + fadeOut() },
                    popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }) + fadeIn() },
                    popExitTransition = { slideOutHorizontally(targetOffsetX = { -it }) + fadeOut() }
                ) {
                    ProfileScreen(
                        onNavigateBack = { 
                            safeBack()
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
                    ContactScreen(onBack = { 
                        if (navController.previousBackStackEntry != null) {
                            navController.popBackStack() 
                        } else {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    })
                }

                composable(
                    route = "${Screen.InviteCaretaker.route}?groupId={groupId}",
                    arguments = listOf(navArgument("groupId") { type = NavType.StringType; nullable = true; defaultValue = null })
                ) { backStackEntry ->
                    val gid = backStackEntry.arguments?.read { getString("groupId") }
                    
                    // Root interceptor for this specific route to prevent exit
                    com.commu.luklan.utils.CommonBackHandler(enabled = navController.previousBackStackEntry == null) {
                         navController.navigate(Screen.Home.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }

                    InviteCaretakerScreen(
                        groupId = gid,
                        onBack = { 
                            safeBack()
                        }
                    )
                }

                composable(Screen.CaretakerDashboard.route) {
                    CaretakerDashboardScreen(
                        onBack = { 
                            safeBack()
                        },
                        onRefresh = {
                            // Global watchdog sync on dashboard refresh
                            scope.launch {
                                authRepository.getCurrentUserId()?.let { uid ->
                                    authRepository.getUserProfile(uid).onSuccess { profile ->
                                        if (profile.role == "caretaker") {
                                            val groupRepository = getGroupRepository()
                                            val medicineRepository = getMedicineRepository()
                                            val notificationScheduler = getNotificationScheduler()
                                            
                                            groupRepository.getGroupsForUser(profile.id).onSuccess { groups ->
                                                groups.forEach { group ->
                                                    groupRepository.getGroupMembers(group.id).onSuccess { members ->
                                                        members.forEach { member ->
                                                            if (member.id != profile.id && member.role == "patient") {
                                                                scope.launch {
                                                                    medicineRepository.observeMedicines(member.id).collectLatest { result ->
                                                                        result.onSuccess { medicines ->
                                                                            medicines.forEach { med ->
                                                                                notificationScheduler.schedule(med)
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
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
                        onBack = { 
                            safeBack()
                        },
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
                        onBack = { 
                            safeBack()
                        },
                        onSuccess = { gid ->
                            if (gid.isNotEmpty()) {
                                selectedGroupId = gid
                                // Get group name if possible, otherwise empty
                                navController.navigate(Screen.GroupMembers.route) {
                                    popUpTo(Screen.JoinGroup.route) { inclusive = true }
                                }
                            } else {
                                navController.navigate(Screen.CaretakerDashboard.route) {
                                    popUpTo(Screen.JoinGroup.route) { inclusive = true }
                                }
                            }
                        }
                    )
                }

                composable(Screen.CreateGroup.route) {
                    CreateGroupScreen(
                        onBack = { 
                            safeBack()
                        },
                        onSuccess = {
                            navController.navigate(Screen.CaretakerDashboard.route) {
                                popUpTo(Screen.CreateGroup.route) { inclusive = true }
                            }
                        }
                    )
                }

                composable(Screen.AddMedicine.route) {
                    AddMedicineScreen(
                        targetUserId = selectedPatientId,
                        onNavigateBack = { 
                            safeBack()
                        }
                    )
                }

                composable(
                    route = "${Screen.MedicineDetail.route}/{date}",
                    arguments = listOf(navArgument("date") { type = NavType.StringType })
                ) { backStackEntry ->
                    val date = backStackEntry.arguments?.read { getString("date") } ?: ""
                    medicineToEdit?.let { medicine ->
                        MedicineDetailScreen(
                            medicine = medicine,
                            initialSlotTime = activeDeepLinkTime,
                            selectedDate = date,
                            onBack = { 
                                if (navController.previousBackStackEntry != null) {
                                    navController.popBackStack()
                                } else {
                                    navController.navigate(Screen.Home.route) {
                                        popUpTo(0) { inclusive = true }
                                    }
                                }
                            },
                            onEdit = { navController.navigate(Screen.EditMedicine.route) },
                            onMedicineTaken = { },
                            onDeepLinkConsumed = { activeDeepLinkTime = null }
                        )
                    } ?: LaunchedEffect(Unit) {
                        // Safety fallback if medicine data is missing
                        if (!navController.popBackStack()) {
                             navController.navigate(Screen.Home.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    }
                }

                composable(Screen.EditMedicine.route) {
                    medicineToEdit?.let { medicine ->
                        EditMedicineScreen(
                            medicine = medicine,
                            onNavigateBack = { updated ->
                                medicineToEdit = updated ?: medicine
                                if (navController.previousBackStackEntry != null) {
                                    navController.popBackStack()
                                } else {
                                    navController.navigate(Screen.Home.route) {
                                        popUpTo(0) { inclusive = true }
                                    }
                                }
                            }
                        )
                    } ?: LaunchedEffect(Unit) {
                        if (!navController.popBackStack()) {
                             navController.navigate(Screen.Home.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    }
                }

                composable(Screen.History.route) {
                    HistoryScreen(
                        targetUserId = selectedTargetUserIdForNotif,
                        onBack = { 
                            safeBack()
                        },
                        onNavigateToSummary = { uid ->
                            navController.navigate("${Screen.MedSummary.route}?userId=$uid&userName=${selectedPatientName ?: ""}")
                        }
                    )
                }

                composable(Screen.MedicineGroups.route) {
                    MedicineGroupsScreen(
                        targetUserId = selectedTargetUserIdForNotif,
                        onBack = { 
                            safeBack()
                        },
                        onMedicineClick = { medicine ->
                            medicineToEdit = medicine
                            val now = Instant.fromEpochMilliseconds(getCurrentTimeMillis()).toLocalDateTime(TimeZone.currentSystemDefault())
                            val todayStr = "${now.year}-${now.monthNumber.toString().padStart(2, '0')}-${now.dayOfMonth.toString().padStart(2, '0')}"
                            navController.navigate("${Screen.MedicineDetail.route}/$todayStr")
                        }
                    )
                }

                composable(
                    route = Screen.NotificationCenter.route,
                    enterTransition = { slideInHorizontally(initialOffsetX = { it }) + fadeIn() },
                    exitTransition = { slideOutHorizontally(targetOffsetX = { it }) + fadeOut() },
                    popEnterTransition = { slideInHorizontally(initialOffsetX = { it }) + fadeIn() },
                    popExitTransition = { slideOutHorizontally(targetOffsetX = { it }) + fadeOut() }
                ) {
                    NotificationCenterScreen(
                        targetUserId = selectedTargetUserIdForNotif,
                        onBack = { 
                            safeBack()
                        },
                        onMedicineClick = { medicine ->
                            medicineToEdit = medicine
                            val now = Instant.fromEpochMilliseconds(getCurrentTimeMillis()).toLocalDateTime(TimeZone.currentSystemDefault())
                            val todayStr = "${now.year}-${now.monthNumber.toString().padStart(2, '0')}-${now.dayOfMonth.toString().padStart(2, '0')}"
                            navController.navigate("${Screen.MedicineDetail.route}/$todayStr")
                        }
                    )
                }
            }
        }
    }
}
