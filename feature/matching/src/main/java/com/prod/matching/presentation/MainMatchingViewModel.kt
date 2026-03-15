package com.prod.matching.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prod.common.api.model.ApiErrorReason
import com.prod.common.api.model.ApiOperationResult
import com.prod.matching.domain.model.MatchAction
import com.prod.matching.domain.usecase.GetMatchingFeedUseCase
import com.prod.matching.domain.usecase.SubmitMatchActionUseCase
import kotlinx.coroutines.launch

class MainMatchingViewModel(
	private val getMatchingFeedUseCase: GetMatchingFeedUseCase,
	private val submitMatchActionUseCase: SubmitMatchActionUseCase,
	private val recommendationUiMapper: RecommendationUiMapper
) : ViewModel() {

	private var isLoadMoreInFlight = false

	var uiState by mutableStateOf(MainMatchingUiState())
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
		advanceProfile()
	}

	private fun loadFeed() {
		viewModelScope.launch {
			uiState = uiState.copy(isLoading = true, error = null)
			when (val result = getMatchingFeedUseCase()) {
				is ApiOperationResult.Success -> {
					val profiles = recommendationUiMapper.mapRecommendations(result.data)
					uiState = uiState.copy(
						isLoading = false,
						error = null,
						profiles = profiles,
						currentIndex = 0
					)
				}

				is ApiOperationResult.ValidationError -> {
					uiState = uiState.copy(
						isLoading = false,
						error = result.message.orDefaultErrorMessage()
					)
				}

				is ApiOperationResult.ApiError -> {
					uiState = uiState.copy(
						isLoading = false,
						error = result.toMessage()
					)
				}

				ApiOperationResult.NoInternet -> {
					uiState = uiState.copy(
						isLoading = false,
						error = "Нет подключения к интернету"
					)
				}
			}
		}
	}

	private fun submitAction(action: MatchAction) {
		val currentProfile = uiState.currentProfile ?: return
		advanceProfile()
		viewModelScope.launch {
			when (
				val result = submitMatchActionUseCase(
					profileId = currentProfile.id,
					action = action,
					recommendationId = currentProfile.recommendationId
				)
			) {
				null -> Unit
				is ApiOperationResult.Success -> Unit
				is ApiOperationResult.ValidationError -> {
					uiState = uiState.copy(error = result.message.orDefaultErrorMessage())
				}

				is ApiOperationResult.ApiError -> {
					uiState = uiState.copy(error = result.toMessage())
				}

				ApiOperationResult.NoInternet -> {
					uiState = uiState.copy(error = "Нет подключения к интернету")
				}
			}
		}
	}

	private fun advanceProfile() {
		val nextIndex = uiState.currentIndex + 1
		uiState = uiState.copy(
			currentIndex = nextIndex,
			isActionsSheetVisible = false,
			error = null
		)
		loadMoreIfNeeded()
	}

	private fun loadMoreIfNeeded() {
		val remainingItems = uiState.profiles.size - uiState.currentIndex - 1
		if (remainingItems > 2 || isLoadMoreInFlight || uiState.isLoading) {
			return
		}

		isLoadMoreInFlight = true
		viewModelScope.launch {
			uiState = uiState.copy(isLoadingMore = true)
			when (val result = getMatchingFeedUseCase()) {
				is ApiOperationResult.Success -> {
					val appendedProfiles = recommendationUiMapper.mapRecommendations(result.data)
					val knownRecommendationIds = uiState.profiles.mapNotNull { it.recommendationId }.toHashSet()
					val newProfiles = appendedProfiles.filterNot { profile ->
						profile.recommendationId != null && profile.recommendationId in knownRecommendationIds
					}
					uiState = uiState.copy(
						isLoadingMore = false,
						profiles = uiState.profiles + newProfiles
					)
				}

				is ApiOperationResult.ValidationError -> {
					uiState = uiState.copy(
						isLoadingMore = false,
						error = result.message.orDefaultErrorMessage()
					)
				}

				is ApiOperationResult.ApiError -> {
					uiState = uiState.copy(
						isLoadingMore = false,
						error = result.toMessage()
					)
				}

				ApiOperationResult.NoInternet -> {
					uiState = uiState.copy(
						isLoadingMore = false,
						error = "Нет подключения к интернету"
					)
				}
			}
			isLoadMoreInFlight = false
		}
	}

	private fun ApiOperationResult.ApiError.toMessage(): String {
		return message.orDefaultErrorMessage(
			fallback = when (reason) {
				ApiErrorReason.Unauthorized -> "Сессия истекла, войдите снова"
				ApiErrorReason.ServiceUnavailable -> "Сервис временно недоступен"
				ApiErrorReason.RequestFailed -> "Не удалось выполнить запрос"
			}
		)
	}

	private fun String?.orDefaultErrorMessage(
		fallback: String = "Произошла ошибка"
	): String {
		return this?.takeIf { it.isNotBlank() } ?: fallback
	}
}
