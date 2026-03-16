package com.prod.matching.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prod.common.api.model.ApiErrorReason
import com.prod.common.api.model.ApiOperationResult
import com.prod.common.api.model.sanitizeUserFacingApiMessage
import com.prod.matching.domain.interactor.DrainResult
import com.prod.matching.domain.interactor.MatchingDeckCoordinator
import com.prod.matching.domain.interactor.PendingMatchActionQueue
import com.prod.matching.domain.model.MatchAction
import com.prod.matching.domain.model.PendingMatchAction
import com.prod.matching.domain.usecase.GetMatchingFeedUseCase
import com.prod.matching.domain.usecase.GetRecommendationsUseCase
import com.prod.matching.domain.usecase.SubmitMatchActionUseCase
import com.prod.matching.model.MatchingDeckItem
import com.prod.matching.model.MatchingDeckItem.Profile
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex

class MainMatchingViewModel(
	private val getMatchingFeedUseCase: GetMatchingFeedUseCase,
	private val getRecommendationsUseCase: GetRecommendationsUseCase,
	private val submitMatchActionUseCase: SubmitMatchActionUseCase,
	private val recommendationUiMapper: RecommendationUiMapper,
	private val deckCoordinator: MatchingDeckCoordinator,
	private val pendingActionQueue: PendingMatchActionQueue
) : ViewModel() {

	// Prevents parallel pagination requests when the user reaches the end of the current deck.
	private val loadMoreMutex = Mutex()
	// Ensures the pending reactions queue is drained by a single coroutine at a time.
	private val pendingActionsMutex = Mutex()
	// Guards one-time recovery of persisted actions and initial feed loading.
	private var isInitialized = false
	private var lastAppliedReloadToken = 0
	// Navigation callback is injected from the screen because ViewModel must not own navigation.
	private var unauthorizedHandler: (() -> Unit)? = null
	private var noInternetHandler: (((() -> Unit)) -> Unit)? = null
	private var nextPendingDeckActionId = 0L

	var uiState by mutableStateOf(MainMatchingUiState())
		private set

	fun initialize(
		onUnauthorized: () -> Unit,
		onNoInternet: (retryAction: () -> Unit) -> Unit,
		reloadToken: Int = 0
	) {
		unauthorizedHandler = onUnauthorized
		noInternetHandler = onNoInternet
		if (isInitialized) {
			return
		}

		isInitialized = true
		lastAppliedReloadToken = reloadToken
		// Recover optimistic actions from the previous process before talking to the network.
		pendingActionQueue.restore()
		loadFeed()
		// Start draining immediately so pending reactions are not stuck until the next swipe.
		processPendingActions()
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

	fun retryRecommendations() {
		loadMoreIfNeeded(force = true)
	}

	fun submitProfileAction(
		userId: String,
		action: MatchAction
	) {
		val currentProfile = uiState.currentProfile
		if (currentProfile?.id != userId || uiState.pendingDeckAction != null) {
			return
		}

		uiState = uiState.copy(
			isActionsSheetVisible = false,
			error = null,
			pendingDeckAction = PendingDeckAction(
				userId = userId,
				action = action,
				requestId = ++nextPendingDeckActionId
			)
		)
	}

	fun consumePendingDeckAction() {
		val pendingDeckAction = uiState.pendingDeckAction ?: return
		if (uiState.currentProfile?.id != pendingDeckAction.userId) {
			uiState = uiState.copy(pendingDeckAction = null)
			return
		}

		uiState = uiState.copy(pendingDeckAction = null)
		submitAction(pendingDeckAction.action)
	}

	fun refreshForReloadToken(reloadToken: Int) {
		if (!isInitialized || reloadToken == lastAppliedReloadToken) {
			return
		}

		lastAppliedReloadToken = reloadToken
		resetDeckState()
		loadFeed()
	}

	fun refreshAfterTargetingChange() {
		if (!isInitialized) {
			return
		}

		resetDeckState()
		loadFeed()
	}

	private fun loadFeed() {
		viewModelScope.launch {
			uiState = uiState.copy(isLoading = true, error = null)
			when (val result = getMatchingFeedUseCase()) {
				is ApiOperationResult.Success -> {
					uiState = uiState.copy(
						isLoading = false,
						error = null,
						items = deckCoordinator.replaceFeed(
							incomingLikes = recommendationUiMapper.mapIncomingLikes(result.data.incomingLikes),
							recommendations = recommendationUiMapper.mapRecommendations(result.data.recommendations)
						),
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
					if (result.reason == ApiErrorReason.Unauthorized) {
						uiState = uiState.copy(isLoading = false, error = null)
						handleUnauthorized()
						return@launch
					}

					uiState = uiState.copy(
						isLoading = false,
						error = result.toMessage()
					)
				}

				ApiOperationResult.NoInternet -> {
					uiState = uiState.copy(isLoading = false, error = null)
					handleNoInternet {
						loadFeed()
					}
				}
			}
		}
	}

	private fun submitAction(action: MatchAction) {
		val currentProfile = uiState.currentProfile ?: return
		if (action == MatchAction.Skip) {
			advanceProfile()
			return
		}

		pendingActionQueue.enqueue(
			PendingMatchAction(
				profileId = currentProfile.id,
				recommendationId = currentProfile.recommendationId,
				action = action
			)
		)
		if (action == MatchAction.Ban) {
			removeProfileFromDeck(currentProfile.id)
		} else {
			advanceProfile()
		}
		processPendingActions()
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

	private fun removeProfileFromDeck(profileId: String) {
		val deckUpdate = deckCoordinator.removeProfile(
			profileId = profileId,
			preferredStartIndex = uiState.currentIndex
		)
		uiState = uiState.copy(
			items = deckUpdate.items,
			currentIndex = deckUpdate.nextProfileIndex,
			isActionsSheetVisible = false,
			error = null
		)
		loadMoreIfNeeded()
	}

	private fun processPendingActions() {
		if (pendingActionQueue.isEmpty() || !pendingActionsMutex.tryLock()) {
			return
		}

		viewModelScope.launch {
			try {
				when (
					val result = pendingActionQueue.drain { pendingAction ->
						submitMatchActionUseCase(
							profileId = pendingAction.profileId,
							action = pendingAction.action,
							recommendationId = pendingAction.recommendationId
						)
					}
				) {
					DrainResult.Completed -> Unit
					is DrainResult.ValidationError -> {
						uiState = uiState.copy(error = result.message.orDefaultErrorMessage())
					}
					is DrainResult.ApiError -> {
						if (result.result.reason == ApiErrorReason.Unauthorized) {
							uiState = uiState.copy(error = null)
							pendingActionQueue.clear()
							handleUnauthorized()
						} else {
							uiState = uiState.copy(error = "Не удалось синхронизировать реакцию")
						}
					}
					DrainResult.NoInternet -> {
						uiState = uiState.copy(error = null)
						handleNoInternet {
							processPendingActions()
						}
					}
				}
			} finally {
				pendingActionsMutex.unlock()
			}
		}
	}

	private fun loadMoreIfNeeded(force: Boolean = false) {
		val remainingItems = uiState.items.size - uiState.currentIndex - 1
		if ((!force && remainingItems > 2) || uiState.isLoading || !loadMoreMutex.tryLock()) {
			return
		}

		viewModelScope.launch {
			try {
				uiState = uiState.copy(isLoadingMore = true)
				when (val result = getRecommendationsUseCase()) {
					is ApiOperationResult.Success -> {
						uiState = uiState.copy(
							isLoadingMore = false,
							items = deckCoordinator.appendRecommendations(
								recommendationUiMapper.mapRecommendations(result.data)
							)
						)
					}

					is ApiOperationResult.ValidationError -> {
						uiState = uiState.copy(
							isLoadingMore = false,
							error = result.message.orDefaultErrorMessage()
						)
					}

					is ApiOperationResult.ApiError -> {
						if (result.reason == ApiErrorReason.Unauthorized) {
							uiState = uiState.copy(isLoadingMore = false, error = null)
							handleUnauthorized()
							return@launch
						}

						uiState = uiState.copy(
							isLoadingMore = false,
							error = result.toMessage()
						)
					}

					ApiOperationResult.NoInternet -> {
						uiState = uiState.copy(isLoadingMore = false, error = null)
						handleNoInternet {
							loadMoreIfNeeded()
						}
					}
				}
			} finally {
				loadMoreMutex.unlock()
			}
		}
	}

	private fun handleUnauthorized() {
		unauthorizedHandler?.invoke()
	}

	private fun handleNoInternet(retryAction: () -> Unit) {
		noInternetHandler?.invoke(retryAction)
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
		return sanitizeUserFacingApiMessage() ?: fallback
	}

	private fun resetDeckState() {
		deckCoordinator.clear()
		uiState = uiState.copy(
			items = emptyList(),
			currentIndex = 0,
			isActionsSheetVisible = false,
			pendingDeckAction = null,
			error = null
		)
	}
}
