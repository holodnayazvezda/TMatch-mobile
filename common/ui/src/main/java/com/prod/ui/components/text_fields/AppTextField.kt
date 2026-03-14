package com.prod.ui.components.text_fields

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTextField(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    value: String,
    onValueChange: (String) -> Unit,
    readOnly: Boolean = false,
    label: String = "",
    focusedPlaceholder: String? = null,
    error: String? = null,
    singleLine: Boolean = true,
    maxLines: Int = 1,
    keyboardOptions: KeyboardOptions = KeyboardOptions(
        keyboardType = KeyboardType.Text,
        imeAction = ImeAction.Next
    ),
    visualTransformation: VisualTransformation = VisualTransformation.None,
    onDone: (() -> Unit)? = null,
    trailingIcon: @Composable () -> Unit = {}
) {
    val typography = MaterialTheme.typography
    val colorScheme = MaterialTheme.colorScheme

    var isFocused by remember { mutableStateOf(false) }

    // Умное управление состоянием текста для корректной установки курсора в конец при внешнем изменении (подсказки)
    var textFieldValue by remember { mutableStateOf(TextFieldValue(value)) }
    LaunchedEffect(value) {
        if (textFieldValue.text != value) {
            textFieldValue = textFieldValue.copy(
                text = value,
                selection = TextRange(value.length)
            )
        }
    }

    Box(
        Modifier.height(73.dp),
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(59.dp)
                .offset(x = 5.dp)
                .background(
                    color = colorScheme.primaryContainer,
                    shape = RoundedCornerShape(15.dp)
                )
        )
        OutlinedTextField(
            modifier = modifier
                .fillMaxWidth()
                .offset(y = 5.dp)
                .onFocusChanged { isFocused = it.isFocused },
            value = textFieldValue,
            readOnly = readOnly,
            onValueChange = {
                textFieldValue = it
                onValueChange(it.text)
            },
            textStyle = typography.labelLarge.copy(fontSize = 14.sp),
            visualTransformation = visualTransformation,
            placeholder = {
                Text(
                    text = if (isFocused && !focusedPlaceholder.isNullOrEmpty()) focusedPlaceholder else label,
                    style = typography.labelLarge,
                    fontSize = 14.sp,
                )
            },
            isError = error != null,
            supportingText = {
                if (error != null) {
                    Spacer(Modifier.height(5.dp))
                    Text(
                        text = error,
                        style = typography.labelLarge,
                        fontSize = 12.sp
                    )
                }
            },
            keyboardOptions = keyboardOptions,
            keyboardActions = KeyboardActions(
                onDone = {
                    onDone?.invoke()
                }
            ),
            singleLine = singleLine,
            maxLines = maxLines,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = colorScheme.surfaceContainerHighest,
                unfocusedContainerColor = colorScheme.surfaceContainerHighest,
                errorContainerColor = colorScheme.errorContainer,

                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                errorBorderColor = Color.Transparent,

                focusedPlaceholderColor = colorScheme.onSurfaceVariant,
                unfocusedPlaceholderColor = colorScheme.onSurfaceVariant,
                errorPlaceholderColor = colorScheme.onErrorContainer,

                focusedTextColor = colorScheme.onSurface,
                unfocusedTextColor = colorScheme.onSurface,
                errorTextColor = colorScheme.onErrorContainer,

                cursorColor = colorScheme.primary,
                errorCursorColor = colorScheme.onErrorContainer
            ),
            shape = RoundedCornerShape(15.dp),
            trailingIcon = trailingIcon
        )

        if (readOnly && onClick != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable {onClick()}
            )
        }
    }
}
