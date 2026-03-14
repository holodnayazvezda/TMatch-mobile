package com.prod.auth.presentation.signup.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.prod.auth.R
import com.prod.ui.components.text_fields.AppPasswordTextField
import com.prod.ui.components.text_fields.AppTextField

@Composable
fun SignUpCredentialsForm(
	email: String,
	password: String,
	confirmPassword: String,
	emailError: String?,
	passwordError: String?,
	confirmPasswordError: String?,
	onEmailChange: (String) -> Unit,
	onPasswordChange: (String) -> Unit,
	onConfirmPasswordChange: (String) -> Unit,
	onDone: () -> Unit
) {
	AppTextField(
		value = email,
		onValueChange = onEmailChange,
		label = stringResource(R.string.auth_email),
		error = emailError,
		keyboardOptions = KeyboardOptions(
			keyboardType = KeyboardType.Email,
			imeAction = ImeAction.Next
		)
	)
	Spacer(modifier = Modifier.height(12.dp))
	AppPasswordTextField(
		value = password,
		onValueChange = onPasswordChange,
		label = stringResource(R.string.auth_password_min_length),
		error = passwordError,
		keyboardOptions = KeyboardOptions(
			keyboardType = KeyboardType.Password,
			imeAction = ImeAction.Next
		)
	)
	Spacer(modifier = Modifier.height(12.dp))
	AppPasswordTextField(
		value = confirmPassword,
		onValueChange = onConfirmPasswordChange,
		label = stringResource(R.string.auth_confirm_password),
		error = confirmPasswordError,
		onDone = onDone
	)
}
