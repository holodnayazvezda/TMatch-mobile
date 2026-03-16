package com.prod.auth.presentation.signup

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.prod.auth.R
import com.prod.common.api.model.UserSession
import com.prod.auth.presentation.common.AuthFormErrorCard
import com.prod.auth.presentation.common.AuthScreenHeader
import com.prod.auth.presentation.common.asString
import com.prod.auth.presentation.common.asStringOrNull
import com.prod.auth.presentation.signup.components.SignUpCredentialsForm
import com.prod.auth.presentation.signup.components.SignUpJuryShortcutButton
import com.prod.ui.components.buttons.AppBigButton
import com.prod.ui.components.buttons.AppMediumButton
import com.prod.ui.components.AppScrollableScreen
import com.prod.ui.components.AppSectionCard
import org.koin.androidx.compose.koinViewModel

@Composable
fun SignUpScreen(
	onBack: () -> Unit,
	onSwitchToSignIn: (String) -> Unit,
	onAuthSuccess: suspend (UserSession) -> Unit,
	onRequestNoInternet: (retryAction: () -> Unit) -> Unit,
	prefilledEmail: String = "",
	modifier: Modifier = Modifier,
	viewModel: SignUpViewModel = koinViewModel()
) {
	val uiState = viewModel.uiState

	LaunchedEffect(prefilledEmail) {
		viewModel.applyPrefilledEmail(prefilledEmail)
	}

	AppScrollableScreen(
		modifier = modifier.fillMaxSize()
	) {
		AuthScreenHeader(
			title = stringResource(R.string.auth_sign_up_title),
			description = stringResource(R.string.auth_sign_up_description),
			onBack = onBack,
			modifier = Modifier.fillMaxWidth()
		)
		SignUpJuryShortcutButton(
			onClick = {
				viewModel.submitJuryRegistration(
					onSuccess = onAuthSuccess,
					onNoInternet = onRequestNoInternet
				)
			},
			enabled = !uiState.isLoading,
			modifier = Modifier.align(Alignment.End)
		)
		AppSectionCard {
			SignUpCredentialsForm(
				email = uiState.email,
				password = uiState.password,
				confirmPassword = uiState.confirmPassword,
				emailError = uiState.emailError.asStringOrNull(),
				passwordError = uiState.passwordError.asStringOrNull(),
				confirmPasswordError = uiState.confirmPasswordError.asStringOrNull(),
				onEmailChange = viewModel::onEmailChange,
				onPasswordChange = viewModel::onPasswordChange,
				onConfirmPasswordChange = viewModel::onConfirmPasswordChange,
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
				buttonText = stringResource(R.string.auth_sign_up_button),
				isLoading = uiState.isLoading,
				onClick = {
					viewModel.submit(
						onSuccess = onAuthSuccess,
						onNoInternet = onRequestNoInternet
					)
				}
			)
			AppMediumButton(
				buttonText = stringResource(R.string.auth_switch_to_sign_in),
				isLoading = false,
				onClick = {
					onSwitchToSignIn(uiState.email)
				}
			)
		}
	}
}
