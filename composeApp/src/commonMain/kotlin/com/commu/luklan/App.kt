package com.commu.luklan

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.commu.luklan.data.Medicine
import com.commu.luklan.navigation.Screen
import com.commu.luklan.ui.home.HomeScreen
import com.commu.luklan.ui.login.LoginScreen
import com.commu.luklan.ui.medicine.AddMedicineScreen
import com.commu.luklan.ui.medicine.EditMedicineScreen
import com.commu.luklan.ui.onboarding.OnboardingScreen
import com.commu.luklan.ui.signup.SignupScreen
import com.commu.luklan.ui.splash.SplashScreen
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    MaterialTheme {
        val navController = rememberNavController()
            var medicineToEdit by remember { mutableStateOf<Medicine?>(null) }

        NavHost(navController = navController, startDestination = Screen.Splash.route) {
            composable(Screen.Splash.route) {
                SplashScreen(
                        onNavigateToOnboarding = {
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
                        onNavigateToLogin = {
                            navController.navigate(Screen.Login.route) {
                                popUpTo(Screen.Onboarding.route) { inclusive = true }
                            }
                        },
                        onNavigateToSignup = {
                            navController.navigate(Screen.Signup.route) {
                                popUpTo(Screen.Onboarding.route) { inclusive = true }
                            }
                        }
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
                        onNavigateToLogin = {
                            navController.navigate(Screen.Login.route) {
                                popUpTo(Screen.Signup.route) { inclusive = true }
                            }
                        }
                )
            }

            composable(Screen.Home.route) {
                HomeScreen(
                    onNavigateToAddMedicine = {
                        navController.navigate(Screen.AddMedicine.route)
                    },
                    onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
                    onNavigateToEditMedicine = { medicine ->
                        medicineToEdit = medicine
                        navController.navigate(Screen.EditMedicine.route)
                    }
                )
            }

            composable(Screen.AddMedicine.route) {
                AddMedicineScreen(onNavigateBack = { navController.popBackStack() })
            }

            composable(Screen.EditMedicine.route) {
                medicineToEdit?.let { medicine ->
                    EditMedicineScreen(
                        medicine = medicine,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
            }

            composable(Screen.Profile.route) {
                com.commu.luklan.ui.profile.ProfileScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onLogoutSuccess = {
                            navController.navigate(Screen.Login.route) {
                                popUpTo(0) { inclusive = true } // Clear entire stack
                            }
                        }
                )
            }
        }
    }
}
