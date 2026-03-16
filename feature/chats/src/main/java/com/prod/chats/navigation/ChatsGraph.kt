package com.prod.chats.navigation

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.EntryProviderScope
import com.prod.chats.presentation.ChatsScreen

fun EntryProviderScope<Any>.chatsGraph(
	onUnauthorized: () -> Unit,
	onRequestNoInternet: (retryAction: () -> Unit) -> Unit
) {
	entry<ChatsNavKey> {
		ChatsScreen(
			onUnauthorized = onUnauthorized,
			onRequestNoInternet = onRequestNoInternet
		)
	}
}
