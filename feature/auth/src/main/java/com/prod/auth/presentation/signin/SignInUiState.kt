package com.prod.auth.presentation.signin

data class SignInUiState(
	val email: String = "",
	val password: String = "",
	val emailError: String? = null,
	val passwordError: String? = null,
	val formError: String? = null,
	val isLoading: Boolean = false
)
