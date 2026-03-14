package com.prod.auth.presentation.signin

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.prod.auth.R
import com.prod.common.api.model.UserSession
import com.prod.auth.presentation.common.AuthCredentialsForm
import com.prod.auth.presentation.common.AuthFormErrorCard
import com.prod.auth.presentation.common.AuthScreenHeader
import com.prod.auth.presentation.common.asString
import com.prod.auth.presentation.common.asStringOrNull
import com.prod.ui.components.AppGradientBackground
import com.prod.ui.components.buttons.AppBigButton
import com.prod.ui.components.buttons.AppMediumButton
import androidx.compose.foundation.layout.safeDrawingPadding
import org.koin.androidx.compose.koinViewModel

@Composable
fun SignInScreen(
	onBack: () -> Unit,
	onSwitchToSignUp: (String) -> Unit,
	onAuthSuccess: (UserSession) -> Unit,
	onRequestNoInternet: (retryAction: () -> Unit) -> Unit,
	prefilledEmail: String = "",
	modifier: Modifier = Modifier,
	viewModel: SignInViewModel = koinViewModel()
) {
	val uiState = viewModel.uiState

	LaunchedEffect(prefilledEmail) {
		viewModel.applyPrefilledEmail(prefilledEmail)
	}

	AppGradientBackground(modifier = modifier) {
		Column(
			modifier = Modifier
				.fillMaxSize()
				.verticalScroll(rememberScrollState())
				.safeDrawingPadding()
				.padding(horizontal = 24.dp, vertical = 16.dp)
		) {
			AuthScreenHeader(
				title = stringResource(R.string.auth_sign_in_title),
				description = stringResource(R.string.auth_sign_in_description),
				onBack = onBack
			)
			Spacer(modifier = Modifier.height(28.dp))
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
				Spacer(modifier = Modifier.height(20.dp))
				AuthFormErrorCard(message = message.asString())
			}
			Spacer(modifier = Modifier.height(24.dp))
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
			Spacer(modifier = Modifier.height(12.dp))
			AppMediumButton(
				buttonText = stringResource(R.string.auth_switch_to_sign_up),
				isLoading = false,
				onClick = {
					onSwitchToSignUp(uiState.email)
				}
			)
		}
	}
}
