package com.prod.chats.navigation

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.EntryProviderScope
import com.prod.chats.presentation.ChatScreen
import com.prod.chats.presentation.ChatsScreen

fun EntryProviderScope<Any>.chatsGraph(
	onBack: () -> Unit,
	onOpenChat: (chatId: String) -> Unit,
	onUnauthorized: () -> Unit,
	onRequestNoInternet: (retryAction: () -> Unit) -> Unit
) {
	entry<ChatsNavKey> {
		ChatsScreen(
			onOpenChat = onOpenChat,
			onUnauthorized = onUnauthorized,
			onRequestNoInternet = onRequestNoInternet
		)
	}
	entry<ChatNavKey> { key ->
		ChatScreen(
			chatId = key.chatId,
			onBack = onBack,
			onUnauthorized = onUnauthorized,
			onRequestNoInternet = onRequestNoInternet
		)
	}
}
