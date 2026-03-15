package com.prod.matching.presentation

import com.prod.matching.model.MatchProfile

data class MainMatchingUiState(
	val profiles: List<MatchProfile> = emptyList(),
	val currentIndex: Int = 0,
	val isActionsSheetVisible: Boolean = false
) {
	val currentProfile: MatchProfile?
		get() = profiles.getOrNull(currentIndex)

	val nextProfile: MatchProfile?
		get() = profiles.getOrNull(currentIndex + 1)
}
