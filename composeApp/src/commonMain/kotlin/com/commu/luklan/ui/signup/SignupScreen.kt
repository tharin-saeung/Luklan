package com.commu.luklan.ui.signup

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import com.commu.luklan.data.AuthRepository
import com.commu.luklan.ui.theme.LuklanTheme
import com.commu.luklan.ui.theme.LuklanTypography
import kotlinx.coroutines.launch

@Composable
fun SignupScreen(onNavigateToHome: () -> Unit, onNavigateToLogin: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()
    val authRepository = remember { AuthRepository() }
    val scrollState = rememberScrollState()

    Column(
            modifier =
                    Modifier.fillMaxSize()
                            .background(LuklanTheme.colors.Background)
                            .padding(LuklanTheme.spacing.xl)
                            .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(LuklanTheme.spacing.xxl))

        Text(
                text = "สมัครสมาชิก",
                style = LuklanTheme.typography.h1,
                color = LuklanTheme.colors.TextPrimary
        )

        Spacer(modifier = Modifier.height(LuklanTheme.spacing.xxl))

        // Name Input
        OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("ชื่อ") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(LuklanTheme.dimensions.radiusSmall),
                colors =
                        OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = LuklanTheme.colors.Surface,
                                unfocusedContainerColor = LuklanTheme.colors.Surface,
                                focusedBorderColor = LuklanTheme.colors.TextSecondary,
                                unfocusedBorderColor = LuklanTheme.colors.Indicator
                        )
        )

        Spacer(modifier = Modifier.height(LuklanTheme.spacing.md))

        // Email Input
        OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("อีเมล") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                placeholder = { Text("example@email.com") },
                shape = RoundedCornerShape(LuklanTheme.dimensions.radiusSmall),
                colors =
                        OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = LuklanTheme.colors.Surface,
                                unfocusedContainerColor = LuklanTheme.colors.Surface,
                                focusedBorderColor = LuklanTheme.colors.TextSecondary,
                                unfocusedBorderColor = LuklanTheme.colors.Indicator
                        )
        )

        Spacer(modifier = Modifier.height(LuklanTheme.spacing.md))

        // Password Input
        OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("รหัสผ่าน") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                shape = RoundedCornerShape(LuklanTheme.dimensions.radiusSmall),
                colors =
                        OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = LuklanTheme.colors.Surface,
                                unfocusedContainerColor = LuklanTheme.colors.Surface,
                                focusedBorderColor = LuklanTheme.colors.TextSecondary,
                                unfocusedBorderColor = LuklanTheme.colors.Indicator
                        )
        )

        Spacer(modifier = Modifier.height(LuklanTheme.spacing.md))

        // Confirm Password Input
        OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("ยืนยันรหัสผ่าน") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                shape = RoundedCornerShape(LuklanTheme.dimensions.radiusSmall),
                colors =
                        OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = LuklanTheme.colors.Surface,
                                unfocusedContainerColor = LuklanTheme.colors.Surface,
                                focusedBorderColor = LuklanTheme.colors.TextSecondary,
                                unfocusedBorderColor = LuklanTheme.colors.Indicator
                        )
        )

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(LuklanTheme.spacing.sm))
            Text(
                    text = errorMessage!!,
                    color = LuklanTheme.colors.Error,
                    style = LuklanTypography.bodyMedium
            )
        }

        Spacer(modifier = Modifier.height(LuklanTheme.spacing.xl))

        Button(
                onClick = {
                    when {
                        name.isBlank() || email.isBlank() || password.isBlank() -> {
                            errorMessage = "กรุณากรอกข้อมูลให้ครบถ้วน"
                        }
                        password != confirmPassword -> {
                            errorMessage = "รหัสผ่านไม่ตรงกัน"
                        }
                        password.length < 6 -> {
                            errorMessage = "รหัสผ่านต้องมีอย่างน้อย 6 ตัวอักษร"
                        }
                        else -> {
                            isLoading = true
                            errorMessage = null
                            scope.launch {
                                val result = authRepository.signUpWithEmail(email, password, name)
                                isLoading = false
                                if (result.isSuccess) {
                                    onNavigateToHome()
                                } else {
                                    errorMessage = "สมัครสมาชิกไม่สำเร็จ: ${result.exceptionOrNull()?.message}"
                                }
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(LuklanTheme.dimensions.buttonLarge),
                shape = RoundedCornerShape(LuklanTheme.dimensions.radiusLarge),
                colors = ButtonDefaults.buttonColors(containerColor = LuklanTheme.colors.Primary),
                enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = LuklanTheme.colors.OnPrimary)
            } else {
                Text(
                        text = "สมัครสมาชิก",
                        style = LuklanTypography.bodyLarge,
                        color = LuklanTheme.colors.OnPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(LuklanTheme.spacing.md))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                    text = "มีบัญชีอยู่แล้ว? ",
                    style = LuklanTypography.bodyMedium,
                    color = LuklanTheme.colors.TextSecondary
            )
            Text(
                    text = "เข้าสู่ระบบ",
                    style = LuklanTypography.bodyMedium,
                    color = LuklanTheme.colors.Primary,
                    modifier = Modifier.clickable { onNavigateToLogin() }
            )
        }
        
        Spacer(modifier = Modifier.height(LuklanTheme.spacing.xl))
    }
}