package com.prod.ui.components.text_fields

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.prod.ui.R

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
	keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
	visualTransformation: VisualTransformation = VisualTransformation.None,
	dropdownItems: List<T> = emptyList(),
	onDropdownItemSelected: (T) -> Unit = {},
	dropDownItem: @Composable (T) -> Unit,
	trailingIcon: @Composable (Boolean) -> Unit = { expanded ->
		val rotation by animateFloatAsState(
			targetValue = if (expanded) 180f else 0f,
			label = "dropdown_icon_rotation"
		)
		Icon(
			modifier = Modifier.rotate(rotation),
			painter = painterResource(R.drawable.outline_arrow_drop_down_24),
			tint = MaterialTheme.colorScheme.primary,
			contentDescription = null
		)
	}
) {
	var expanded by remember { mutableStateOf(false) }
	var anchorWidth by remember { mutableStateOf(0.dp) }
	val density = LocalDensity.current

	Box(modifier = modifier.fillMaxWidth()) {
		Box(
			modifier = Modifier
				.fillMaxWidth()
				.onGloballyPositioned { coordinates ->
					anchorWidth = with(density) { coordinates.size.width.toDp() }
				}
		) {
			AppTextField(
				modifier = Modifier.fillMaxWidth(),
				value = value,
				onValueChange = onValueChange,
				readOnly = readOnly,
				onClick = if (readOnly && dropdownItems.isNotEmpty()) {
					{ expanded = true }
				} else {
					null
				},
				label = label,
				focusedPlaceholder = focusedPlaceholder,
				error = error,
				singleLine = singleLine,
				maxLines = maxLines,
				keyboardOptions = keyboardOptions,
				visualTransformation = visualTransformation,
				trailingIcon = { trailingIcon(expanded) }
			)
		}

		DropdownMenu(
			expanded = expanded,
			onDismissRequest = { expanded = false },
			modifier = Modifier.dropdownWidth(anchorWidth)
		) {
			if (dropdownItems.isEmpty()) {
				DropdownMenuItem(
					text = {
						Text(
							text = stringResource(R.string.common_dropdown_empty),
							style = MaterialTheme.typography.titleMedium
						)
					},
					onClick = { expanded = false }
				)
			} else {
				dropdownItems.forEach { item ->
					DropdownMenuItem(
						text = { dropDownItem(item) },
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

private fun Modifier.dropdownWidth(width: Dp): Modifier {
	return if (width > 0.dp) {
		this.then(Modifier.width(width))
	} else {
		this
	}
}
