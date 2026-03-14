package com.prod.auth.presentation.onboarding

import androidx.annotation.StringRes
import com.prod.auth.presentation.onboarding.model.OnboardingPageUiModel

data class OnboardingUiState(
	@param:StringRes val appTitleRes: Int,
	val pages: List<OnboardingPageUiModel>
)
