package com.prod.matching.presentation

import com.prod.match.domain.model.TargetGender

data class TargetingPreferencesUiState(
	val isLoading: Boolean = true,
	val isSaving: Boolean = false,
	val error: String? = null,
	val region: String? = null,
	val genderTarget: TargetGender = TargetGender.Both,
	val ageFrom: Int = 18,
	val ageTo: Int = 30
) {
	val ageRangeLabel: String
		get() = "$ageFrom-$ageTo"
}
