package com.prod.auth.presentation.onboarding

import com.prod.auth.presentation.onboarding.model.OnboardingPageUiModel

data class OnboardingUiState(
	val appTitle: String,
	val pages: List<OnboardingPageUiModel>
)
