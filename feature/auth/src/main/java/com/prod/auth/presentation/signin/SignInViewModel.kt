package com.prod.auth.presentation.signin

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prod.auth.domain.usecase.SignInWithEmailUseCase
import com.prod.auth.domain.validation.EmailValidator
import com.prod.auth.domain.validation.PasswordValidator
import com.prod.auth.presentation.common.AuthOperationResultUiMapper
import com.prod.common.api.model.ApiOperationResult
import com.prod.common.api.model.UserSession
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
		onSuccess: suspend (UserSession) -> Unit,
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
				is ApiOperationResult.Success -> {
					onSuccess(result.data)
					uiState = uiState.copy(isLoading = false)
				}

				is ApiOperationResult.ValidationError -> {
					uiState = uiState.copy(
						isLoading = false,
						formError = authOperationResultUiMapper.mapValidationError(result)
					)
				}

				is ApiOperationResult.ApiError -> {
					uiState = uiState.copy(
						isLoading = false,
						formError = authOperationResultUiMapper.mapApiError(result)
					)
				}

				ApiOperationResult.NoInternet -> {
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
