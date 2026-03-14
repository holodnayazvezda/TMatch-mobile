package com.prod.auth.presentation.signin

import com.prod.auth.presentation.common.UiText

data class SignInUiState(
	val email: String = "",
	val password: String = "",
	val emailError: UiText? = null,
	val passwordError: UiText? = null,
	val formError: UiText? = null,
	val isLoading: Boolean = false
)
