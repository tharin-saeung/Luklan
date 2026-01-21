package com.commu.luklan.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Onboarding : Screen("onboarding")
    object Login : Screen("login")
    object Signup : Screen("signup")
    object Home : Screen("home")
    object AddMedicine : Screen("add_medicine")
    object EditMedicine : Screen("edit_medicine")
    object MedicineList : Screen("medicine_list")
    object Profile : Screen("profile")
    object InviteCaretaker : Screen("invite_caretaker")
    object QRScanner : Screen("qr_scanner")
    object CaretakerDashboard : Screen("caretaker_dashboard")
}