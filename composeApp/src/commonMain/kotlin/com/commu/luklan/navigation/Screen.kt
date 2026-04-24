package com.commu.luklan.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Onboarding : Screen("onboarding")
    object Login : Screen("login")
    object Signup : Screen("signup")
    object Home : Screen("home")
    object AddMedicine : Screen("add_medicine")
    object EditMedicine : Screen("edit_medicine")
    object MedicineDetail : Screen("medicine_detail")
    object MedicineList : Screen("medicine_list")
    object Profile : Screen("profile")
    object History : Screen("history")
    object MedicineGroups : Screen("medicine_groups")
    object AddMethod : Screen("add_method")
    object OcrScan : Screen("ocr_scan")
    object InviteCaretaker : Screen("invite_caretaker")
    object QRScanner : Screen("qr_scanner") // Renaming internally to JoinGroup
    object JoinGroup : Screen("join_group")
    object CaretakerDashboard : Screen("caretaker_dashboard") // Renaming internally to GroupList
    object GroupMembers : Screen("group_members")
    object PatientTimeline : Screen("patient_timeline")
    object CreateGroup : Screen("create_group")
    object NotificationCenter : Screen("notification_center")
    object Contact : Screen("contact")
}
