package com.prod.chats.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prod.chats.domain.usecase.GetOwnChatsUseCase
import com.prod.common.api.model.ApiErrorReason
import com.prod.common.api.model.ApiOperationResult
import com.prod.common.api.model.sanitizeUserFacingApiMessage
import kotlinx.coroutines.launch

class ChatsViewModel(
	private val getOwnChatsUseCase: GetOwnChatsUseCase,
	private val chatPreviewUiMapper: ChatPreviewUiMapper
) : ViewModel() {

	var uiState by mutableStateOf(ChatsUiState())
		private set

	private var isInitialized = false

	fun initialize(
		onUnauthorized: () -> Unit,
		onNoInternet: (retryAction: () -> Unit) -> Unit
	) {
		if (isInitialized) return
		isInitialized = true
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
			uiState = uiState.copy(isLoading = true, error = null)

			when (val result = getOwnChatsUseCase()) {
				is ApiOperationResult.Success -> {
					uiState = uiState.copy(
						isLoading = false,
						items = chatPreviewUiMapper.map(result.data),
						error = null
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
						uiState = uiState.copy(isLoading = false)
						onUnauthorized()
						return@launch
					}

					uiState = uiState.copy(
						isLoading = false,
						error = result.toMessage()
					)
				}

				ApiOperationResult.NoInternet -> {
					uiState = uiState.copy(isLoading = false)
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
