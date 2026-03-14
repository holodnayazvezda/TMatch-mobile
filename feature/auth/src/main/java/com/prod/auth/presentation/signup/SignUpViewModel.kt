package com.prod.auth.presentation.signup

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prod.auth.domain.model.SignUpCredentials
import com.prod.auth.domain.model.AuthOperationResult
import com.prod.auth.domain.model.AuthSession
import com.prod.auth.domain.usecase.CreateJurySignUpCredentialsUseCase
import com.prod.auth.domain.usecase.SignUpWithEmailUseCase
import com.prod.auth.domain.validation.ConfirmPasswordValidator
import com.prod.auth.domain.validation.EmailValidator
import com.prod.auth.domain.validation.PasswordValidator
import com.prod.auth.presentation.common.AuthOperationResultUiMapper
import kotlinx.coroutines.launch

class SignUpViewModel(
	private val signUpWithEmailUseCase: SignUpWithEmailUseCase,
	private val createJurySignUpCredentialsUseCase: CreateJurySignUpCredentialsUseCase,
	private val emailValidator: EmailValidator,
	private val passwordValidator: PasswordValidator,
	private val confirmPasswordValidator: ConfirmPasswordValidator,
	private val authOperationResultUiMapper: AuthOperationResultUiMapper
) : ViewModel() {

	var uiState by mutableStateOf(SignUpUiState())
		private set

	fun onEmailChange(value: String) {
		uiState = uiState.copy(
			email = value,
			emailError = null,
			formError = null
		)
	}

	fun onPasswordChange(value: String) {
		uiState = uiState.copy(
			password = value,
			passwordError = null,
			confirmPasswordError = null,
			formError = null
		)
	}

	fun onConfirmPasswordChange(value: String) {
		uiState = uiState.copy(
			confirmPassword = value,
			confirmPasswordError = null,
			formError = null
		)
	}

	fun applyPrefilledEmail(value: String) {
		if (value.isBlank() || uiState.email.isNotBlank()) {
			return
		}

		uiState = uiState.copy(
			email = value,
			emailError = null,
			formError = null
		)
	}

	fun submit(
		onSuccess: (AuthSession) -> Unit,
		onNoInternet: (retryAction: () -> Unit) -> Unit
	) {
		submitCredentials(
			credentials = currentCredentials(),
			onSuccess = onSuccess,
			onNoInternet = onNoInternet
		)
	}

	fun submitJuryRegistration(
		onSuccess: (AuthSession) -> Unit,
		onNoInternet: (retryAction: () -> Unit) -> Unit
	) {
		val credentials = createJurySignUpCredentialsUseCase()
		applyCredentials(credentials)
		submitCredentials(
			credentials = credentials,
			onSuccess = onSuccess,
			onNoInternet = onNoInternet
		)
	}

	private fun currentCredentials(): SignUpCredentials {
		return SignUpCredentials(
			email = uiState.email,
			password = uiState.password,
			confirmPassword = uiState.confirmPassword
		)
	}

	private fun applyCredentials(credentials: SignUpCredentials) {
		uiState = uiState.copy(
			email = credentials.email,
			password = credentials.password,
			confirmPassword = credentials.confirmPassword,
			emailError = null,
			passwordError = null,
			confirmPasswordError = null,
			formError = null
		)
	}

	// Обычная форма и shortcut для жюри проходят через один и тот же путь валидации и отправки.
	private fun submitCredentials(
		credentials: SignUpCredentials,
		onSuccess: (AuthSession) -> Unit,
		onNoInternet: (retryAction: () -> Unit) -> Unit
	) {
		val emailError = emailValidator.validate(credentials.email)
		val passwordError = passwordValidator.validateForSignUp(credentials.password)
		val confirmPasswordError = confirmPasswordValidator.validate(
			password = credentials.password,
			confirmPassword = credentials.confirmPassword
		)

		uiState = uiState.copy(
			emailError = emailError,
			passwordError = passwordError,
			confirmPasswordError = confirmPasswordError,
			formError = null
		)

		if (emailError != null || passwordError != null || confirmPasswordError != null) {
			return
		}

		viewModelScope.launch {
			uiState = uiState.copy(isLoading = true)
			when (val result = signUpWithEmailUseCase(credentials.email.trim(), credentials.password)) {
				is AuthOperationResult.Success -> {
					uiState = uiState.copy(isLoading = false)
					onSuccess(result.session)
				}

				is AuthOperationResult.ValidationError -> {
					uiState = uiState.copy(
						isLoading = false,
						formError = authOperationResultUiMapper.mapValidationError(result)
					)
				}

				is AuthOperationResult.ApiError -> {
					uiState = uiState.copy(
						isLoading = false,
						formError = authOperationResultUiMapper.mapApiError(result)
					)
				}

				AuthOperationResult.NoInternet -> {
					uiState = uiState.copy(isLoading = false)
					onNoInternet {
						submitCredentials(
							credentials = credentials,
							onSuccess = onSuccess,
							onNoInternet = onNoInternet
						)
					}
				}
			}
		}
	}
}
