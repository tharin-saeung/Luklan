package com.commu.luklan.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.commu.luklan.ui.theme.LuklanTheme.LuklanTypography
import com.commu.luklan.ui.theme.LuklanTheme.LuklanColors

@Composable
fun OtpTextField(
    otpText: String,
    onOtpTextChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    otpCount: Int = 5
) {
    var textFieldValue by remember(otpText) {
        mutableStateOf(
            TextFieldValue(
                text = otpText,
                selection = TextRange(otpText.length)
            )
        )
    }
    val focusRequester = remember { FocusRequester() }

    BasicTextField(
        value = textFieldValue,
        onValueChange = {
            if (it.text.length <= otpCount) {
                textFieldValue = it.copy(text = it.text.uppercase())
                onOtpTextChange(it.text.uppercase())
            }
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            capitalization = KeyboardCapitalization.Characters
        ),
        modifier = modifier
            .fillMaxWidth()
            .focusRequester(focusRequester)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                focusRequester.requestFocus()
            },
        decorationBox = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                repeat(otpCount) { index ->
                    val char = when {
                        index >= textFieldValue.text.length -> ""
                        else -> textFieldValue.text[index].toString()
                    }
                    val isFocused = textFieldValue.selection.collapsed && textFieldValue.selection.start == index

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(60.dp)
                            .background(
                                color = LuklanColors.Surface,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .border(
                                width = if (isFocused) 2.dp else 1.dp,
                                color = if (isFocused) LuklanColors.Primary else LuklanColors.Indicator,
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = char,
                            style = LuklanTypography.h2,
                            fontWeight = FontWeight.Bold,
                            color = LuklanColors.TextPrimary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    )
}
