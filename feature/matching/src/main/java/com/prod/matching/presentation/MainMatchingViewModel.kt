package com.prod.matching.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prod.common.api.model.ApiErrorReason
import com.prod.common.api.model.ApiOperationResult
import com.prod.matching.domain.gateway.PendingMatchActionStore
import com.prod.matching.domain.model.MatchAction
import com.prod.matching.domain.model.PendingMatchAction
import com.prod.matching.domain.usecase.GetMatchingFeedUseCase
import com.prod.matching.domain.usecase.GetRecommendationsUseCase
import com.prod.matching.domain.usecase.SubmitMatchActionUseCase
import com.prod.matching.model.MatchProfile
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import java.util.ArrayDeque

class MainMatchingViewModel(
	private val getMatchingFeedUseCase: GetMatchingFeedUseCase,
	private val getRecommendationsUseCase: GetRecommendationsUseCase,
	private val submitMatchActionUseCase: SubmitMatchActionUseCase,
	private val recommendationUiMapper: RecommendationUiMapper,
	private val pendingMatchActionStore: PendingMatchActionStore
) : ViewModel() {

	// Prevents parallel pagination requests when the user reaches the end of the current deck.
	private val loadMoreMutex = Mutex()
	// Ensures the pending reactions queue is drained by a single coroutine at a time.
	private val pendingActionsMutex = Mutex()
	// Guards one-time recovery of persisted actions and initial feed loading.
	private var isInitialized = false
	// Navigation callback is injected from the screen because ViewModel must not own navigation.
	private var unauthorizedHandler: (() -> Unit)? = null
	private var noInternetHandler: (((() -> Unit)) -> Unit)? = null
	// In-memory mirror of persisted optimistic reactions waiting to be sent to the backend.
	private val pendingActions = ArrayDeque<PendingMatchAction>()

	var uiState by mutableStateOf(MainMatchingUiState())
		private set

	fun initialize(
		onUnauthorized: () -> Unit,
		onNoInternet: (retryAction: () -> Unit) -> Unit
	) {
		unauthorizedHandler = onUnauthorized
		noInternetHandler = onNoInternet
		if (isInitialized) {
			return
		}

		isInitialized = true
		// Recover optimistic actions from the previous process before talking to the network.
		restorePendingActions()
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

	fun submitProfileAction(
		userId: String,
		action: MatchAction
	) {
		val currentProfile = uiState.currentProfile
		if (currentProfile?.id != userId) {
			return
		}

		submitAction(action)
	}

	private fun loadFeed() {
		viewModelScope.launch {
			uiState = uiState.copy(isLoading = true, error = null)
			when (val result = getMatchingFeedUseCase()) {
				is ApiOperationResult.Success -> {
					val incomingLikeProfiles = recommendationUiMapper.mapIncomingLikes(result.data.incomingLikes)
					val incomingLikeUserIds = incomingLikeProfiles.mapTo(hashSetOf(), MatchProfile::id)
					val recommendationProfiles = recommendationUiMapper
						.mapRecommendations(result.data.recommendations)
						.filterNot { it.id in incomingLikeUserIds }
					uiState = uiState.copy(
						isLoading = false,
						error = null,
						profiles = incomingLikeProfiles + recommendationProfiles,
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
		pendingActions.addLast(
			PendingMatchAction(
				profileId = currentProfile.id,
				recommendationId = currentProfile.recommendationId,
				action = action
			)
		)
		// Persist before the UI advances so the swipe survives process death.
		syncPendingActions()
		advanceProfile()
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

	private fun processPendingActions() {
		if (pendingActions.isEmpty() || !pendingActionsMutex.tryLock()) {
			return
		}

		viewModelScope.launch {
			try {
				// Deliver reactions in order to avoid reordering user intent across retries.
				while (pendingActions.isNotEmpty()) {
					val pendingAction = pendingActions.first()
					when (
						val result = submitMatchActionUseCase(
							profileId = pendingAction.profileId,
							action = pendingAction.action,
							recommendationId = pendingAction.recommendationId
						)
					) {
						null,
						is ApiOperationResult.Success -> {
							pendingActions.removeFirst()
							syncPendingActions()
						}

						is ApiOperationResult.ValidationError -> {
							pendingActions.removeFirst()
							syncPendingActions()
							uiState = uiState.copy(error = result.message.orDefaultErrorMessage())
						}

						is ApiOperationResult.ApiError -> {
							if (result.reason == ApiErrorReason.Unauthorized) {
								uiState = uiState.copy(error = null)
								pendingActions.clear()
								syncPendingActions()
								handleUnauthorized()
								break
							}

							uiState = uiState.copy(
								error = "Не удалось синхронизировать реакцию"
							)
							// Keep the failed item in storage and retry later from the same queue head.
							break
						}

						ApiOperationResult.NoInternet -> {
							uiState = uiState.copy(error = null)
							handleNoInternet {
								processPendingActions()
							}
							break
						}
					}
				}
			} finally {
				pendingActionsMutex.unlock()
			}
		}
	}

	private fun loadMoreIfNeeded() {
		val remainingItems = uiState.profiles.size - uiState.currentIndex - 1
		if (remainingItems > 2 || uiState.isLoading || !loadMoreMutex.tryLock()) {
			return
		}

		viewModelScope.launch {
			try {
				uiState = uiState.copy(isLoadingMore = true)
				when (val result = getRecommendationsUseCase()) {
					is ApiOperationResult.Success -> {
						val appendedProfiles = recommendationUiMapper.mapRecommendations(result.data)
						val knownRecommendationIds = uiState.profiles.mapNotNull { it.recommendationId }.toHashSet()
						val knownUserIds = uiState.profiles.mapTo(hashSetOf(), MatchProfile::id)
						val newProfiles = appendedProfiles.filterNot { profile ->
							profile.id in knownUserIds ||
								(profile.recommendationId != null && profile.recommendationId in knownRecommendationIds)
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

	private fun restorePendingActions() {
		// The in-memory queue is reconstructed on every fresh ViewModel session from local storage.
		pendingActions.clear()
		pendingActions.addAll(pendingMatchActionStore.getAll())
	}

	private fun syncPendingActions() {
		if (pendingActions.isEmpty()) {
			pendingMatchActionStore.clear()
			return
		}

		pendingMatchActionStore.saveAll(pendingActions.toList())
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
