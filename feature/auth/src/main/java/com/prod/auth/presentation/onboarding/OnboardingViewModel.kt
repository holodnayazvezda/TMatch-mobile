package com.prod.auth.presentation.onboarding

import androidx.lifecycle.ViewModel
import com.prod.auth.R
import com.prod.auth.presentation.onboarding.model.OnboardingPageUiModel

class OnboardingViewModel : ViewModel() {
	val uiState = OnboardingUiState(
		appTitleRes = R.string.onboarding_app_title,
		pages = listOf(
			OnboardingPageUiModel(
				labelRes = R.string.onboarding_page_1_label,
				titleRes = R.string.onboarding_page_1_title,
				descriptionRes = R.string.onboarding_page_1_description
			),
			OnboardingPageUiModel(
				labelRes = R.string.onboarding_page_2_label,
				titleRes = R.string.onboarding_page_2_title,
				descriptionRes = R.string.onboarding_page_2_description
			),
			OnboardingPageUiModel(
				labelRes = R.string.onboarding_page_3_label,
				titleRes = R.string.onboarding_page_3_title,
				descriptionRes = R.string.onboarding_page_3_description
			),
			OnboardingPageUiModel(
				labelRes = R.string.onboarding_page_4_label,
				titleRes = R.string.onboarding_page_4_title,
				descriptionRes = R.string.onboarding_page_4_description
			)
		)
	)
}
