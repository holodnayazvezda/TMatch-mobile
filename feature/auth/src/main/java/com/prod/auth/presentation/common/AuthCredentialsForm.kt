package com.prod.auth.presentation.common

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.prod.ui.components.text_fields.AppTextField

@Composable
fun AuthCredentialsForm(
	email: String,
	password: String,
	emailError: String?,
	passwordError: String?,
	passwordLabel: String,
	onEmailChange: (String) -> Unit,
	onPasswordChange: (String) -> Unit,
	onDone: () -> Unit
) {
	AppTextField(
		value = email,
		onValueChange = onEmailChange,
		label = "Email",
		error = emailError,
		keyboardOptions = KeyboardOptions(
			keyboardType = KeyboardType.Email,
			imeAction = ImeAction.Next
		)
	)
	Spacer(modifier = Modifier.height(12.dp))
	AppTextField(
		value = password,
		onValueChange = onPasswordChange,
		label = passwordLabel,
		error = passwordError,
		visualTransformation = PasswordVisualTransformation(),
		keyboardOptions = KeyboardOptions(
			keyboardType = KeyboardType.Password,
			imeAction = ImeAction.Done
		),
		onDone = onDone
	)
}
