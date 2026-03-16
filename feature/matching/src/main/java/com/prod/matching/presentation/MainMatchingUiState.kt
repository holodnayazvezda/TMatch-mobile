package com.prod.matching.presentation

import com.prod.matching.model.MatchingDeckItem
import com.prod.matching.model.MatchingDeckItem.Profile

data class PendingDeckAction(
	val userId: String,
	val action: com.prod.matching.domain.model.MatchAction,
	val requestId: Long
)

data class MainMatchingUiState(
	val isLoading: Boolean = false,
	val isLoadingMore: Boolean = false,
	val error: String? = null,
	val items: List<MatchingDeckItem> = emptyList(),
	val currentIndex: Int = 0,
	val isActionsSheetVisible: Boolean = false,
	val pendingDeckAction: PendingDeckAction? = null
) {
	val currentItem: MatchingDeckItem?
		get() = items.getOrNull(currentIndex)

	val nextItem: MatchingDeckItem?
		get() = items.getOrNull(currentIndex + 1)

	val currentProfile: Profile?
		get() = currentItem as? Profile

	val nextProfile: Profile?
		get() = nextItem as? Profile
}
