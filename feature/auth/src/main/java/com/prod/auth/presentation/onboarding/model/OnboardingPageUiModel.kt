package com.prod.auth.presentation.onboarding.model

import androidx.annotation.StringRes

data class OnboardingPageUiModel(
	@param:StringRes val labelRes: Int,
	@param:StringRes val titleRes: Int,
	@param:StringRes val descriptionRes: Int
)
