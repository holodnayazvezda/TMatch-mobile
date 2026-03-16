package com.prod.profile.presentation

data class UserProfileUiState(
	val isLoading: Boolean = false,
	val profileCard: UserProfileCardUiState? = null,
	val error: String? = null
)

data class UserProfileCardUiState(
	val userId: String,
	val fullName: String,
	val age: Int?,
	val birthDateLabel: String,
	val region: String?,
	val avatarUrl: String?,
	val datingPhotoUrls: List<String>,
	val visibleTraitLabels: List<String>
)
