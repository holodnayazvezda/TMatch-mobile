package com.prod.profile.presentation

data class ProfileUiState(
	val isLoading: Boolean = true,
	val isSubmitting: Boolean = false,
	val requiresDatingPhoto: Boolean = false,
	val message: String? = null,
	val error: String? = null
)
