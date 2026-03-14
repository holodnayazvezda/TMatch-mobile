package com.prod.auth.presentation.signup

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.prod.auth.domain.gateway.AuthGateway
import com.prod.auth.domain.model.AuthSession
import com.prod.auth.presentation.common.AuthCredentialsForm
import com.prod.auth.presentation.common.AuthFormErrorCard
import com.prod.auth.presentation.common.AuthScreenHeadline
import com.prod.ui.components.AppGradientBackground
import com.prod.ui.components.buttons.AppBigButton
import com.prod.ui.components.buttons.AppMediumButton

@Composable
fun SignUpScreen(
	authGateway: AuthGateway,
	onBack: () -> Unit,
	onSwitchToSignIn: () -> Unit,
	onAuthSuccess: (AuthSession) -> Unit,
	onRequestNoInternet: (retryAction: () -> Unit) -> Unit,
	modifier: Modifier = Modifier,
	viewModel: SignUpViewModel = viewModel(factory = SignUpViewModel.factory(authGateway))
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
			TextButton(onClick = onBack) {
				Text(text = "Назад")
			}
			Spacer(modifier = Modifier.height(12.dp))
			AuthScreenHeadline(
				title = "Создайте аккаунт",
				description = "Регистрация открывает персональную выдачу, настройки приватности и безопасный сценарий общения."
			)
			Spacer(modifier = Modifier.height(28.dp))
			AuthCredentialsForm(
				email = uiState.email,
				password = uiState.password,
				emailError = uiState.emailError,
				passwordError = uiState.passwordError,
				passwordLabel = "Пароль от 8 символов",
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
				AuthFormErrorCard(message = message)
			}
			Spacer(modifier = Modifier.height(24.dp))
			AppBigButton(
				buttonText = "Зарегистрироваться",
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
				buttonText = "Перейти ко входу",
				isLoading = false,
				onClick = onSwitchToSignIn
			)
		}
	}
}
