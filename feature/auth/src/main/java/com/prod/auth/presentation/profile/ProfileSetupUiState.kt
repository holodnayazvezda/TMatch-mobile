package com.prod.auth.presentation.profile

import com.prod.auth.domain.model.Gender
import com.prod.auth.presentation.common.UiText

data class ProfileSetupUiState(
	val firstName: String = "",
	val lastName: String = "",
	val birthDateIso: String? = null,
	val birthDateDisplay: String = "",
	val gender: Gender? = null,
	val genderDisplay: String = "",
	val region: String = "",
	val firstNameError: UiText? = null,
	val birthDateError: UiText? = null,
	val genderError: UiText? = null,
	val formError: UiText? = null,
	val isCheckingProfile: Boolean = true,
	val isSubmitting: Boolean = false
)
