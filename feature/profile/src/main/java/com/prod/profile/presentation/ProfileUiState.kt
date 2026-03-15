package com.prod.profile.presentation

data class ProfileCardUiState(
	val firstName: String,
	val fullName: String,
	val age: Int?,
	val birthDateLabel: String,
	val region: String?,
	val datingPhotoUrls: List<String>
)

data class ProfileUiState(
	val isLoading: Boolean = true,
	val isSubmitting: Boolean = false,
	val profileCard: ProfileCardUiState? = null,
	val error: String? = null
)
