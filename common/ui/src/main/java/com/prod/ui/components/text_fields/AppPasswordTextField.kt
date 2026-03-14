package com.prod.ui.components.text_fields

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.prod.ui.R

@Composable
fun AppPasswordTextField(
	value: String,
	onValueChange: (String) -> Unit,
	label: String,
	error: String? = null,
	onDone: (() -> Unit)? = null,
	keyboardOptions: KeyboardOptions = KeyboardOptions(
		keyboardType = KeyboardType.Password,
		imeAction = ImeAction.Done
	),
	modifier: Modifier = Modifier
) {
	var isPasswordVisible by remember { mutableStateOf(false) }

	AppTextField(
		modifier = modifier,
		value = value,
		onValueChange = onValueChange,
		label = label,
		error = error,
		keyboardOptions = keyboardOptions,
		visualTransformation = if (isPasswordVisible) {
			VisualTransformation.None
		} else {
			PasswordVisualTransformation()
		},
		onDone = onDone,
		trailingIcon = {
			IconButton(
				modifier = Modifier.size(40.dp),
				onClick = { isPasswordVisible = !isPasswordVisible }
			) {
				Icon(
					modifier = Modifier.size(22.dp),
					painter = painterResource(
						id = if (isPasswordVisible) R.drawable.eye_closed else R.drawable.eye
					),
					contentDescription = if (isPasswordVisible) {
						stringResource(R.string.common_password_hide)
					} else {
						stringResource(R.string.common_password_show)
					},
					tint = MaterialTheme.colorScheme.primary
				)
			}
		}
	)
}
