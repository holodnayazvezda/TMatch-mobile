package com.prod.ui.components.text_fields

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prod.ui.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> AppTextFieldWithDropdown(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit = {},
    readOnly: Boolean = true,
    label: String,
    focusedPlaceholder: String? = null,
    error: String? = null,
    singleLine: Boolean = true,
    maxLines: Int = 1,
    keyboardOptions: KeyboardOptions = KeyboardOptions(
        keyboardType = KeyboardType.Text,
        imeAction = ImeAction.Next
    ),
    visualTransformation: VisualTransformation = VisualTransformation.None,
    dropdownItems: List<T> = emptyList(),
    onDropdownItemSelected: (T) -> Unit = {},
    dropDownItem: @Composable (T) -> Unit,
    trailingIcon: @Composable (Boolean) -> Unit = {
        Icon(
            modifier = Modifier
                .size(24.dp)
                .rotate(animateFloatAsState(if (it) 180f else 0f).value),
            painter = painterResource(R.drawable.outline_arrow_drop_down_24),
            tint = MaterialTheme.colorScheme.primary,
            contentDescription = null
        )
    }
) {
    val typography = MaterialTheme.typography
    val colorScheme = MaterialTheme.colorScheme

    var expanded by remember { mutableStateOf(false) }
    var isFocused by remember { mutableStateOf(false) }

    var textFieldValue by remember { mutableStateOf(TextFieldValue(value)) }
    if (!readOnly) {
        LaunchedEffect(value) {
            if (textFieldValue.text != value) {
                textFieldValue = textFieldValue.copy(
                    text = value,
                    selection = TextRange(value.length)
                )
            }
        }
    }

    Box(
        modifier.height(73.dp),
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

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.offset(y = 5.dp)
        ) {
            val anchorType = if (readOnly) ExposedDropdownMenuAnchorType.PrimaryNotEditable else ExposedDropdownMenuAnchorType.PrimaryEditable

            if (readOnly) {
                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(anchorType)
                        .onFocusChanged { isFocused = it.isFocused },
                    value = value,
                    readOnly = true,
                    onValueChange = onValueChange,
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
                    trailingIcon = {
                        trailingIcon(expanded)
                    }
                )
            } else {
                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(anchorType)
                        .onFocusChanged { isFocused = it.isFocused },
                    value = textFieldValue,
                    readOnly = false,
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
                    trailingIcon = {
                        trailingIcon(expanded)
                    }
                )
            }

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.exposedDropdownSize()
            ) {
                if (dropdownItems.isEmpty()) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                stringResource(R.string.common_dropdown_empty),
                                style = MaterialTheme.typography.titleMedium
                            )
                        },
                        onClick = {
                            expanded = false
                        }
                    )
                }
                dropdownItems.forEach { item ->
                    DropdownMenuItem(
                        text = {
                            dropDownItem(item)
                        },
                        onClick = {
                            onDropdownItemSelected(item)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}
