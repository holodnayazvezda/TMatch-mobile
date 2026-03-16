package com.prod.auth.presentation.signin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.prod.auth.R
import com.prod.auth.domain.model.JuryPreviewUser
import com.prod.common.api.model.UserSession
import com.prod.auth.presentation.common.AuthCredentialsForm
import com.prod.auth.presentation.common.AuthFormErrorCard
import com.prod.auth.presentation.common.AuthScreenHeader
import com.prod.auth.presentation.common.asString
import com.prod.auth.presentation.common.asStringOrNull
import com.prod.auth.presentation.signup.components.SignUpJuryShortcutButton
import com.prod.ui.components.buttons.AppBigButton
import com.prod.ui.components.buttons.AppMediumButton
import com.prod.ui.components.AppScrollableScreen
import com.prod.ui.components.AppSectionCard
import org.koin.androidx.compose.koinViewModel

@Composable
fun SignInScreen(
	modifier: Modifier = Modifier,
	onBack: () -> Unit,
	onSwitchToSignUp: (String) -> Unit,
	onAuthSuccess: suspend (UserSession) -> Unit,
	onRequestNoInternet: (retryAction: () -> Unit) -> Unit,
	prefilledEmail: String = "",
	viewModel: SignInViewModel = koinViewModel()
) {
	val uiState = viewModel.uiState

	LaunchedEffect(prefilledEmail) {
		viewModel.applyPrefilledEmail(prefilledEmail)
	}

	AppScrollableScreen(
		modifier = modifier
	) {
		AuthScreenHeader(
			title = stringResource(R.string.auth_sign_in_title),
			description = stringResource(R.string.auth_sign_in_description),
			onBack = onBack
		)
		SignUpJuryShortcutButton(
			onClick = {
				viewModel.loadJuryPreviewUsers(onNoInternet = onRequestNoInternet)
			},
			enabled = !uiState.isLoading && !uiState.isJuryLoading,
			modifier = Modifier.align(Alignment.End)
		)
		AppSectionCard {
			AuthCredentialsForm(
				email = uiState.email,
				password = uiState.password,
				emailError = uiState.emailError.asStringOrNull(),
				passwordError = uiState.passwordError.asStringOrNull(),
				passwordLabelRes = R.string.auth_password,
				onEmailChange = viewModel::onEmailChange,
				onPasswordChange = viewModel::onPasswordChange,
				onDone = {
					viewModel.submit(
						onSuccess = onAuthSuccess,
						onNoInternet = onRequestNoInternet
					)
				}
			)
			uiState.formError?.let { message ->
				AuthFormErrorCard(message = message.asString())
			}
			AppBigButton(
				buttonText = stringResource(R.string.auth_sign_in_button),
				isLoading = uiState.isLoading,
				onClick = {
					viewModel.submit(
						onSuccess = onAuthSuccess,
						onNoInternet = onRequestNoInternet
					)
				}
			)
			AppMediumButton(
				buttonText = stringResource(R.string.auth_switch_to_sign_up),
				isLoading = false,
				onClick = {
					onSwitchToSignUp(uiState.email)
				}
			)
		}
	}

	if (uiState.isJuryPickerVisible) {
		SignInJuryUserPickerDialog(
			users = uiState.juryPreviewUsers,
			onDismiss = viewModel::dismissJuryPicker,
			onUserSelected = { user ->
				viewModel.applyJuryPreviewUser(user)
				viewModel.submit(
					onSuccess = onAuthSuccess,
					onNoInternet = onRequestNoInternet
				)
			}
		)
	}
}

@Composable
private fun SignInJuryUserPickerDialog(
	users: List<JuryPreviewUser>,
	onDismiss: () -> Unit,
	onUserSelected: (JuryPreviewUser) -> Unit
) {
	AlertDialog(
		onDismissRequest = onDismiss,
		title = {
			Text(text = stringResource(R.string.auth_sign_in_jury_picker_title))
		},
		text = {
			if (users.isEmpty()) {
				Text(text = stringResource(R.string.auth_sign_in_jury_picker_empty))
			} else {
				Column(
					modifier = Modifier.fillMaxWidth(),
					verticalArrangement = Arrangement.spacedBy(8.dp)
				) {
					users.forEach { user ->
						TextButton(
							onClick = { onUserSelected(user) },
							modifier = Modifier.fillMaxWidth()
						) {
							Column(
								modifier = Modifier.fillMaxWidth()
							) {
								Text(text = user.toDisplayName())
								Text(text = user.toPreviewEmail())
							}
						}
					}
				}
			}
		},
		confirmButton = {},
		dismissButton = {
			TextButton(onClick = onDismiss) {
				Text(text = stringResource(R.string.auth_sign_in_jury_picker_cancel))
			}
		}
	)
}

private fun JuryPreviewUser.toDisplayName(): String {
	return listOf(firstName, lastName)
		.filterNot { it.isNullOrBlank() }
		.joinToString(" ")
}

private fun JuryPreviewUser.toPreviewEmail(): String {
	val localPart = listOf(firstName, lastName.orEmpty())
		.map { part ->
			part.trim()
				.lowercase()
				.replace(' ', '_')
		}
		.filter { it.isNotBlank() }
		.joinToString("_")

	return "$localPart@example.com"
}
