package com.prod.auth.presentation.signin

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prod.auth.domain.model.JuryPreviewUser
import com.prod.auth.domain.usecase.GetJuryPreviewUsersUseCase
import com.prod.auth.domain.usecase.SignInWithEmailUseCase
import com.prod.auth.domain.validation.EmailValidator
import com.prod.auth.domain.validation.PasswordValidator
import com.prod.auth.presentation.common.AuthOperationResultUiMapper
import com.prod.common.api.model.ApiOperationResult
import com.prod.common.api.model.UserSession
import kotlinx.coroutines.launch

class SignInViewModel(
	private val signInWithEmailUseCase: SignInWithEmailUseCase,
	private val getJuryPreviewUsersUseCase: GetJuryPreviewUsersUseCase,
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

	fun loadJuryPreviewUsers(
		onNoInternet: (retryAction: () -> Unit) -> Unit
	) {
		viewModelScope.launch {
			uiState = uiState.copy(
				isJuryLoading = true,
				formError = null
			)
			when (val result = getJuryPreviewUsersUseCase()) {
				is ApiOperationResult.Success -> {
					uiState = uiState.copy(
						isJuryLoading = false,
						isJuryPickerVisible = true,
						juryPreviewUsers = result.data,
						formError = null
					)
				}

				is ApiOperationResult.ValidationError -> {
					uiState = uiState.copy(
						isJuryLoading = false,
						formError = authOperationResultUiMapper.mapValidationError(result)
					)
				}

				is ApiOperationResult.ApiError -> {
					uiState = uiState.copy(
						isJuryLoading = false,
						formError = authOperationResultUiMapper.mapApiError(result)
					)
				}

				ApiOperationResult.NoInternet -> {
					uiState = uiState.copy(isJuryLoading = false)
					onNoInternet {
						loadJuryPreviewUsers(onNoInternet = onNoInternet)
					}
				}
			}
		}
	}

	fun dismissJuryPicker() {
		uiState = uiState.copy(isJuryPickerVisible = false)
	}

	fun applyJuryPreviewUser(user: JuryPreviewUser) {
		uiState = uiState.copy(
			email = user.toPreviewEmail(),
			password = JuryPreviewPassword,
			emailError = null,
			passwordError = null,
			formError = null,
			isJuryPickerVisible = false
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

	private companion object {
		const val JuryPreviewPassword = "password"
	}
}
