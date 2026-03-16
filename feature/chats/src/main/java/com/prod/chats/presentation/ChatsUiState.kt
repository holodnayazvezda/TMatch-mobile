package com.prod.chats.presentation

data class ChatsUiState(
	val isLoading: Boolean = false,
	val items: List<ChatListItemUiState> = emptyList(),
	val error: String? = null
)

data class ChatListItemUiState(
	val chatId: String,
	val title: String,
	val subtitle: String,
	val timestamp: String?,
	val accent: ChatAccent,
	val avatarUrl: String?
)

enum class ChatAccent {
	Primary,
	Secondary,
	Tertiary
}
