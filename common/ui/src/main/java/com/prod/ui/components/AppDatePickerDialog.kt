package com.prod.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.prod.ui.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDatePickerDialog(
    onDismissRequest: () -> Unit,
    onDateSelected: (String) -> Unit // Возвращает строку из 8 цифр
) {
    val utcTimeZone = remember { TimeZone.getTimeZone("UTC") }
    val currentYear = remember {
        Calendar.getInstance(utcTimeZone).get(Calendar.YEAR)
    }
    var isInputMode by rememberSaveable { mutableStateOf(true) }
    var selectedDateMillis by rememberSaveable { mutableStateOf<Long?>(null) }
    var displayedMonthMillis by rememberSaveable { mutableStateOf<Long?>(null) }
    var activeSelectedDateMillis by remember { mutableStateOf<Long?>(null) }

    androidx.compose.runtime.key(isInputMode) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDateMillis,
            initialDisplayedMonthMillis = selectedDateMillis ?: displayedMonthMillis,
            yearRange = 1900..currentYear,
            initialDisplayMode = if (isInputMode) {
                DisplayMode.Input
            } else {
                DisplayMode.Picker
            }
        )

        LaunchedEffect(datePickerState) {
            snapshotFlow {
                datePickerState.selectedDateMillis to datePickerState.displayedMonthMillis
            }.collect { (selectedDate, displayedMonth) ->
                selectedDateMillis = selectedDate
                displayedMonthMillis = displayedMonth
                activeSelectedDateMillis = selectedDate
            }
        }

        DatePickerDialog(
            onDismissRequest = onDismissRequest,
            colors = DatePickerDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
            confirmButton = {
                TextButton(
                    onClick = {
                        activeSelectedDateMillis?.let { millis ->
                            val formatter = SimpleDateFormat("ddMMyyyy", Locale.getDefault())
                            formatter.timeZone = utcTimeZone
                            onDateSelected(formatter.format(Date(millis)))
                        }
                        onDismissRequest()
                    }
                ) {
                    Text(
                        text = stringResource(R.string.common_date_picker_confirm),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissRequest) {
                    Text(
                        text = stringResource(R.string.common_date_picker_dismiss),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
                title = {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 24.dp, top = 16.dp, end = 12.dp, bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.common_date_picker_title),
                            style = MaterialTheme.typography.titleMedium
                        )
                        TextButton(
                            onClick = {
                                selectedDateMillis = datePickerState.selectedDateMillis
                                displayedMonthMillis = datePickerState.displayedMonthMillis
                                activeSelectedDateMillis = datePickerState.selectedDateMillis
                                isInputMode = !isInputMode
                            }
                        ) {
                            Text(
                                text = stringResource(
                                    if (isInputMode) {
                                        R.string.common_date_picker_switch_to_calendar
                                    } else {
                                        R.string.common_date_picker_switch_to_input
                                    }
                                ),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                headline = null,
                showModeToggle = false,
                colors = DatePickerDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    headlineContentColor = MaterialTheme.colorScheme.onSurface,
                    weekdayContentColor = MaterialTheme.colorScheme.onSurface,
                    subheadContentColor = MaterialTheme.colorScheme.onSurface,
                    yearContentColor = MaterialTheme.colorScheme.onSurface,
                    currentYearContentColor = MaterialTheme.colorScheme.onSurface,
                    selectedYearContentColor = MaterialTheme.colorScheme.onPrimary,
                    selectedYearContainerColor = MaterialTheme.colorScheme.primary,
                    dayContentColor = MaterialTheme.colorScheme.onSurface,
                    selectedDayContentColor = MaterialTheme.colorScheme.onPrimary,
                    selectedDayContainerColor = MaterialTheme.colorScheme.primary,
                    todayContentColor = MaterialTheme.colorScheme.primary,
                    todayDateBorderColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    }
}
