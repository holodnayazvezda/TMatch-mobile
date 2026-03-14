package com.prod.auth.presentation.signup

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.prod.auth.domain.gateway.AuthGateway
import com.prod.auth.domain.model.AuthOperationResult
import com.prod.auth.domain.model.AuthSession
import com.prod.auth.domain.usecase.SignUpWithEmailUseCase
import com.prod.auth.domain.validation.EmailValidator
import com.prod.auth.domain.validation.PasswordValidator
import kotlinx.coroutines.launch

class SignUpViewModel(
	private val signUpWithEmailUseCase: SignUpWithEmailUseCase,
	private val emailValidator: EmailValidator,
	private val passwordValidator: PasswordValidator
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
			formError = null
		)
	}

	fun submit(
		onSuccess: (AuthSession) -> Unit,
		onNoInternet: (retryAction: () -> Unit) -> Unit
	) {
		val emailError = emailValidator.validate(uiState.email)
		val passwordError = passwordValidator.validateForSignUp(uiState.password)

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
			when (val result = signUpWithEmailUseCase(uiState.email.trim(), uiState.password)) {
				is AuthOperationResult.Success -> {
					uiState = uiState.copy(isLoading = false)
					onSuccess(result.session)
				}

				is AuthOperationResult.ValidationError -> {
					uiState = uiState.copy(
						isLoading = false,
						formError = result.message
					)
				}

				is AuthOperationResult.ApiError -> {
					uiState = uiState.copy(
						isLoading = false,
						formError = result.message
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

	companion object {
		fun factory(authGateway: AuthGateway): ViewModelProvider.Factory {
			return viewModelFactory {
				initializer {
					SignUpViewModel(
						signUpWithEmailUseCase = SignUpWithEmailUseCase(authGateway),
						emailValidator = EmailValidator(),
						passwordValidator = PasswordValidator()
					)
				}
			}
		}
	}
}
