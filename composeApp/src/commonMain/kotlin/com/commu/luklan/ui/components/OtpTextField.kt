package com.commu.luklan.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.commu.luklan.ui.theme.LuklanTheme
import com.commu.luklan.ui.theme.LuklanTypography

@Composable
fun OtpTextField(
        otpText: String,
        onOtpTextChange: (String) -> Unit,
        modifier: Modifier = Modifier
) {
    BasicTextField(
            value = otpText,
            onValueChange = {
                if (it.length <= 6 && it.all { char -> char.isDigit() }) {
                    onOtpTextChange(it)
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            decorationBox = {
                Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                ) {
                    repeat(6) { index ->
                        val char =
                                when {
                                    index >= otpText.length -> ""
                                    else -> otpText[index].toString()
                                }
                        val isFocused = (index == otpText.length)

                        Box(
                                modifier =
                                        Modifier.width(45.dp)
                                                .height(55.dp)
                                                .background(
                                                        color = LuklanTheme.colors.Surface,
                                                        shape =
                                                                RoundedCornerShape(
                                                                        LuklanTheme.dimensions
                                                                                .radiusSmall
                                                                )
                                                )
                                                .border(
                                                        width = if (isFocused) 2.dp else 1.dp,
                                                        color =
                                                                if (isFocused)
                                                                        LuklanTheme.colors.Primary
                                                                else LuklanTheme.colors.Indicator,
                                                        shape =
                                                                RoundedCornerShape(
                                                                        LuklanTheme.dimensions
                                                                                .radiusSmall
                                                                )
                                                ),
                                contentAlignment = Alignment.Center
                        ) {
                            Text(
                                    text = char,
                                    style = LuklanTypography.h3,
                                    color = LuklanTheme.colors.TextPrimary,
                                    textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            },
            modifier = modifier.fillMaxWidth()
    )
}
