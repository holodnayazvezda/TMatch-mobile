package com.prod.chats.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prod.chats.domain.usecase.EnsureChatStreamConnectedUseCase
import com.prod.chats.domain.usecase.ObserveOwnChatsUseCase
import com.prod.chats.domain.usecase.RefreshOwnChatsUseCase
import com.prod.common.api.model.ApiErrorReason
import com.prod.common.api.model.ApiOperationResult
import com.prod.common.api.model.sanitizeUserFacingApiMessage
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ChatsViewModel(
	private val observeOwnChatsUseCase: ObserveOwnChatsUseCase,
	private val refreshOwnChatsUseCase: RefreshOwnChatsUseCase,
	private val ensureChatStreamConnectedUseCase: EnsureChatStreamConnectedUseCase,
	private val chatPreviewUiMapper: ChatPreviewUiMapper
) : ViewModel() {

	var uiState by mutableStateOf(ChatsUiState())
		private set

	private var isInitialized = false
	private var isRefreshing = false
	private var observeChatsJob: Job? = null

	fun initialize(
		onUnauthorized: () -> Unit,
		onNoInternet: (retryAction: () -> Unit) -> Unit
	) {
		if (isInitialized) return
		isInitialized = true
		observeChats()
		viewModelScope.launch { ensureChatStreamConnectedUseCase() }
		reload(onUnauthorized = onUnauthorized, onNoInternet = onNoInternet)
	}

	fun retry(
		onUnauthorized: () -> Unit,
		onNoInternet: (retryAction: () -> Unit) -> Unit
	) {
		reload(onUnauthorized = onUnauthorized, onNoInternet = onNoInternet)
	}

	private fun reload(
		onUnauthorized: () -> Unit,
		onNoInternet: (retryAction: () -> Unit) -> Unit
	) {
		viewModelScope.launch {
			isRefreshing = true
			if (uiState.items.isEmpty()) {
				uiState = uiState.copy(isLoading = true, error = null)
			}

			when (val result = refreshOwnChatsUseCase()) {
				is ApiOperationResult.Success -> {
					isRefreshing = false
					uiState = uiState.copy(isLoading = false, error = null)
				}

				is ApiOperationResult.ValidationError -> {
					handleRefreshFailure(result.message.orDefaultErrorMessage())
				}

				is ApiOperationResult.ApiError -> {
					isRefreshing = false
					if (result.reason == ApiErrorReason.Unauthorized) {
						uiState = uiState.copy(isLoading = false)
						onUnauthorized()
						return@launch
					}

					handleRefreshFailure(result.toMessage())
				}

				ApiOperationResult.NoInternet -> {
					isRefreshing = false
					uiState = uiState.copy(isLoading = false)
					if (uiState.items.isEmpty()) {
						onNoInternet {
							retry(
								onUnauthorized = onUnauthorized,
								onNoInternet = onNoInternet
							)
						}
					}
				}
			}
		}
	}

	private fun observeChats() {
		if (observeChatsJob != null) return

		observeChatsJob = viewModelScope.launch {
			observeOwnChatsUseCase().collectLatest { chats ->
				val mappedItems = chatPreviewUiMapper.map(chats)
				uiState = uiState.copy(
					isLoading = isRefreshing && mappedItems.isEmpty(),
					items = mappedItems,
					error = if (mappedItems.isNotEmpty()) null else uiState.error
				)
			}
		}
	}

	private fun handleRefreshFailure(message: String) {
		isRefreshing = false
		uiState = if (uiState.items.isEmpty()) {
			uiState.copy(isLoading = false, error = message)
		} else {
			uiState.copy(isLoading = false)
		}
	}

	private fun ApiOperationResult.ApiError.toMessage(): String {
		return when (reason) {
			ApiErrorReason.Unauthorized -> "Сессия истекла, войдите снова"
			ApiErrorReason.ServiceUnavailable -> message.orDefaultErrorMessage()
			ApiErrorReason.RequestFailed -> message.orDefaultErrorMessage()
		}
	}

	private fun String?.orDefaultErrorMessage(): String {
		return sanitizeUserFacingApiMessage() ?: "Не удалось загрузить список чатов"
	}
}
