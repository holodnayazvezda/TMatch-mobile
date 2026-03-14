package com.prod.auth.presentation.signup

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.prod.auth.R
import com.prod.auth.domain.model.AuthSession
import com.prod.auth.presentation.common.AuthFormErrorCard
import com.prod.auth.presentation.common.AuthScreenHeader
import com.prod.auth.presentation.common.asString
import com.prod.auth.presentation.common.asStringOrNull
import com.prod.auth.presentation.signup.components.SignUpCredentialsForm
import com.prod.ui.components.AppGradientBackground
import com.prod.ui.components.buttons.AppBigButton
import com.prod.ui.components.buttons.AppMediumButton
import org.koin.androidx.compose.koinViewModel

@Composable
fun SignUpScreen(
	onBack: () -> Unit,
	onSwitchToSignIn: () -> Unit,
	onAuthSuccess: (AuthSession) -> Unit,
	onRequestNoInternet: (retryAction: () -> Unit) -> Unit,
	modifier: Modifier = Modifier,
	viewModel: SignUpViewModel = koinViewModel()
) {
	val uiState = viewModel.uiState

	AppGradientBackground(modifier = modifier) {
		Column(
			modifier = Modifier
				.fillMaxSize()
				.verticalScroll(rememberScrollState())
				.safeDrawingPadding()
				.padding(horizontal = 24.dp, vertical = 16.dp)
		) {
			AuthScreenHeader(
				title = stringResource(R.string.auth_sign_up_title),
				description = stringResource(R.string.auth_sign_up_description),
				onBack = onBack
			)
			Spacer(modifier = Modifier.height(28.dp))
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
				Spacer(modifier = Modifier.height(20.dp))
				AuthFormErrorCard(message = message.asString())
			}
			Spacer(modifier = Modifier.height(24.dp))
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
			Spacer(modifier = Modifier.height(12.dp))
			AppMediumButton(
				buttonText = stringResource(R.string.auth_switch_to_sign_in),
				isLoading = false,
				onClick = onSwitchToSignIn
			)
		}
	}
}
