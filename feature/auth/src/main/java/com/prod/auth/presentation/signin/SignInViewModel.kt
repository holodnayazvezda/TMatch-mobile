package com.prod.auth.presentation.signin

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prod.auth.domain.model.AuthOperationResult
import com.prod.auth.domain.model.AuthSession
import com.prod.auth.domain.usecase.SignInWithEmailUseCase
import com.prod.auth.domain.validation.EmailValidator
import com.prod.auth.domain.validation.PasswordValidator
import com.prod.auth.presentation.common.AuthOperationResultUiMapper
import kotlinx.coroutines.launch

class SignInViewModel(
	private val signInWithEmailUseCase: SignInWithEmailUseCase,
	private val emailValidator: EmailValidator,
	private val passwordValidator: PasswordValidator,
	private val authOperationResultUiMapper: AuthOperationResultUiMapper
) : ViewModel() {

	var uiState by mutableStateOf(SignInUiState())
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
		val emailError = emailValidator.validate(uiState.email)
		val passwordError = passwordValidator.validateForSignIn(uiState.password)

		uiState = uiState.copy(
			emailError = emailError,
			passwordError = passwordError,
			formError = null
		)

		if (emailError != null || passwordError != null) {
			return
		}

		viewModelScope.launch {
			uiState = uiState.copy(isLoading = true)
			when (val result = signInWithEmailUseCase(uiState.email.trim(), uiState.password)) {
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
						submit(
							onSuccess = onSuccess,
							onNoInternet = onNoInternet
						)
					}
				}
			}
		}
	}
}
