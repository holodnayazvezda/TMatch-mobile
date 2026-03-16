package com.prod.chats.presentation

data class ChatUiState(
	val isLoading: Boolean = false,
	val isLoadingMore: Boolean = false,
	val isSending: Boolean = false,
	val title: String = "Чат",
	val avatarUrl: String? = null,
	val currentUserId: String? = null,
	val messages: List<ChatMessageUiState> = emptyList(),
	val draftMessage: String = "",
	val error: String? = null,
	val canLoadMore: Boolean = true
)

data class ChatMessageUiState(
	val messageId: String,
	val text: String,
	val timestamp: String,
	val isOwn: Boolean
)
