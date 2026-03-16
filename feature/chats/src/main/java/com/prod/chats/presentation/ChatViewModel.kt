package com.prod.chats.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prod.auth.domain.gateway.AuthSessionStore
import com.prod.chats.domain.usecase.EnsureChatStreamConnectedUseCase
import com.prod.chats.domain.usecase.ObserveChatMessagesUseCase
import com.prod.chats.domain.usecase.ObserveChatUseCase
import com.prod.chats.domain.usecase.RefreshChatMessagesUseCase
import com.prod.chats.domain.usecase.SendChatMessageUseCase
import com.prod.common.api.model.ApiErrorReason
import com.prod.common.api.model.ApiOperationResult
import com.prod.common.api.model.sanitizeUserFacingApiMessage
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

private const val ChatPageSize = 40

class ChatViewModel(
	authSessionStore: AuthSessionStore,
	private val observeChatUseCase: ObserveChatUseCase,
	private val observeChatMessagesUseCase: ObserveChatMessagesUseCase,
	private val refreshChatMessagesUseCase: RefreshChatMessagesUseCase,
	private val sendChatMessageUseCase: SendChatMessageUseCase,
	private val ensureChatStreamConnectedUseCase: EnsureChatStreamConnectedUseCase,
	private val chatMessageUiMapper: ChatMessageUiMapper
) : ViewModel() {

	var uiState by mutableStateOf(ChatUiState(currentUserId = authSessionStore.get()?.userId))
		private set

	private var isInitialized = false
	private var observeChatJob: Job? = null
	private var observeMessagesJob: Job? = null

	fun initialize(
		chatId: String,
		onUnauthorized: () -> Unit,
		onNoInternet: (retryAction: () -> Unit) -> Unit
	) {
		if (isInitialized) return
		isInitialized = true
		observeChat(chatId)
		observeMessages(chatId)
		viewModelScope.launch { ensureChatStreamConnectedUseCase() }
		refreshInitial(chatId, onUnauthorized, onNoInternet)
	}

	fun onDraftChanged(value: String) {
		uiState = uiState.copy(draftMessage = value)
	}

	fun retry(
		chatId: String,
		onUnauthorized: () -> Unit,
		onNoInternet: (retryAction: () -> Unit) -> Unit
	) {
		viewModelScope.launch { ensureChatStreamConnectedUseCase() }
		refreshInitial(chatId, onUnauthorized, onNoInternet)
	}

	fun sendMessage(
		chatId: String,
		onUnauthorized: () -> Unit,
		onNoInternet: (retryAction: () -> Unit) -> Unit
	) {
		val text = uiState.draftMessage.trim()
		if (text.isBlank() || uiState.isSending) return

		viewModelScope.launch {
			uiState = uiState.copy(isSending = true, error = null)
			when (val result = sendChatMessageUseCase(chatId, text)) {
				is ApiOperationResult.Success -> {
					uiState = uiState.copy(isSending = false, draftMessage = "")
				}

				is ApiOperationResult.ValidationError -> {
					uiState = uiState.copy(
						isSending = false,
						error = result.message.orDefaultErrorMessage()
					)
				}

				is ApiOperationResult.ApiError -> {
					uiState = uiState.copy(isSending = false)
					if (result.reason == ApiErrorReason.Unauthorized) {
						onUnauthorized()
						return@launch
					}
					uiState = uiState.copy(error = result.toMessage())
				}

				ApiOperationResult.NoInternet -> {
					uiState = uiState.copy(isSending = false)
					onNoInternet {
						sendMessage(
							chatId = chatId,
							onUnauthorized = onUnauthorized,
							onNoInternet = onNoInternet
						)
					}
				}
			}
		}
	}

	fun loadOlderMessages(
		chatId: String,
		onUnauthorized: () -> Unit,
		onNoInternet: (retryAction: () -> Unit) -> Unit
	) {
		if (uiState.isLoadingMore || !uiState.canLoadMore) return
		val beforeMessageId = uiState.messages.firstOrNull()?.messageId ?: return

		viewModelScope.launch {
			uiState = uiState.copy(isLoadingMore = true)
			when (
				val result = refreshChatMessagesUseCase(
					chatId = chatId,
					limit = ChatPageSize,
					beforeMessageId = beforeMessageId
				)
			) {
				is ApiOperationResult.Success -> {
					uiState = uiState.copy(
						isLoadingMore = false,
						canLoadMore = result.data.size >= ChatPageSize
					)
				}

				is ApiOperationResult.ValidationError -> {
					uiState = uiState.copy(
						isLoadingMore = false,
						error = result.message.orDefaultErrorMessage()
					)
				}

				is ApiOperationResult.ApiError -> {
					uiState = uiState.copy(isLoadingMore = false)
					if (result.reason == ApiErrorReason.Unauthorized) {
						onUnauthorized()
						return@launch
					}
					uiState = uiState.copy(error = result.toMessage())
				}

				ApiOperationResult.NoInternet -> {
					uiState = uiState.copy(isLoadingMore = false)
					onNoInternet {
						loadOlderMessages(
							chatId = chatId,
							onUnauthorized = onUnauthorized,
							onNoInternet = onNoInternet
						)
					}
				}
			}
		}
	}

	private fun refreshInitial(
		chatId: String,
		onUnauthorized: () -> Unit,
		onNoInternet: (retryAction: () -> Unit) -> Unit
	) {
		viewModelScope.launch {
			uiState = uiState.copy(isLoading = uiState.messages.isEmpty(), error = null)
			when (val result = refreshChatMessagesUseCase(chatId, ChatPageSize)) {
				is ApiOperationResult.Success -> {
					uiState = uiState.copy(
						isLoading = false,
						canLoadMore = result.data.size >= ChatPageSize
					)
				}

				is ApiOperationResult.ValidationError -> {
					uiState = uiState.copy(
						isLoading = false,
						error = result.message.orDefaultErrorMessage()
					)
				}

				is ApiOperationResult.ApiError -> {
					uiState = uiState.copy(isLoading = false)
					if (result.reason == ApiErrorReason.Unauthorized) {
						onUnauthorized()
						return@launch
					}
					uiState = uiState.copy(error = result.toMessage())
				}

				ApiOperationResult.NoInternet -> {
					uiState = uiState.copy(isLoading = false)
					if (uiState.messages.isEmpty()) {
						onNoInternet {
							refreshInitial(chatId, onUnauthorized, onNoInternet)
						}
					}
				}
			}
		}
	}

	private fun observeChat(chatId: String) {
		if (observeChatJob != null) return
		observeChatJob = viewModelScope.launch {
			observeChatUseCase(chatId).collectLatest { chat ->
				uiState = uiState.copy(
					title = chatMessageUiMapper.resolveTitle(chat),
					avatarUrl = chat?.otherUserCard?.profile?.avatarUrl
						?: chat?.otherUserCard?.datingProfile?.photos?.firstOrNull()
				)
			}
		}
	}

	private fun observeMessages(chatId: String) {
		if (observeMessagesJob != null) return
		observeMessagesJob = viewModelScope.launch {
			observeChatMessagesUseCase(chatId).collectLatest { messages ->
				uiState = uiState.copy(
					isLoading = false,
					messages = messages.map { chatMessageUiMapper.mapMessage(it, uiState.currentUserId) }
				)
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
		return sanitizeUserFacingApiMessage() ?: "Не удалось загрузить чат"
	}
}
