package com.prod.auth.presentation.signup

import com.prod.auth.presentation.common.UiText

data class SignUpUiState(
	val email: String = "",
	val password: String = "",
	val confirmPassword: String = "",
	val emailError: UiText? = null,
	val passwordError: UiText? = null,
	val confirmPasswordError: UiText? = null,
	val formError: UiText? = null,
	val isLoading: Boolean = false
)
