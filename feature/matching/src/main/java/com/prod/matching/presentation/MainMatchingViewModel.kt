package com.prod.matching.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prod.matching.domain.model.MatchAction
import com.prod.matching.domain.usecase.GetMatchingFeedUseCase
import com.prod.matching.domain.usecase.SubmitMatchActionUseCase
import com.prod.matching.model.MatchingContentController
import kotlinx.coroutines.launch

class MainMatchingViewModel(
	private val getMatchingFeedUseCase: GetMatchingFeedUseCase,
	private val submitMatchActionUseCase: SubmitMatchActionUseCase
) : ViewModel() {

	var uiState by mutableStateOf(
		MainMatchingUiState(
			profiles = MatchingContentController.profiles()
		)
	)
		private set

	init {
		loadFeed()
	}

	fun onMoreClick() {
		uiState = uiState.copy(isActionsSheetVisible = true)
	}

	fun onDismissSheet() {
		uiState = uiState.copy(isActionsSheetVisible = false)
	}

	fun onSwipeLeft() {
		submitAction(MatchAction.Dislike)
	}

	fun onSwipeRight() {
		submitAction(MatchAction.Like)
	}

	fun onSwipeUp() {
		submitAction(MatchAction.Skip)
	}

	private fun loadFeed() {
		viewModelScope.launch {
			// TODO: replace sample content with getMatchingFeedUseCase() once contracts exist.
			getMatchingFeedUseCase.hashCode()
		}
	}

	private fun submitAction(action: MatchAction) {
		val currentProfile = uiState.currentProfile ?: return
		viewModelScope.launch {
			// TODO: call submitMatchActionUseCase(currentProfile.id, action) when backend flow is ready.
			submitMatchActionUseCase.hashCode()
			currentProfile.id.hashCode()
			action.name.hashCode()
		}
		uiState = uiState.copy(
			currentIndex = (uiState.currentIndex + 1).coerceAtMost(uiState.profiles.lastIndex),
			isActionsSheetVisible = false
		)
	}
}
