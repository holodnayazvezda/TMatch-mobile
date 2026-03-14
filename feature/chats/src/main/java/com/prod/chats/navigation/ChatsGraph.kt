package com.prod.chats.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.prod.ui.components.AppGradientBackground

fun EntryProviderScope<NavKey>.chatsGraph() {
	entry<ChatsNavKey> {
		ChatsScreen()
	}
}

@Composable
private fun ChatsScreen() {
	AppGradientBackground {
		Box(
			modifier = Modifier.fillMaxSize(),
			contentAlignment = Alignment.Center
		) {
			Text(
				text = "Чаты",
				style = MaterialTheme.typography.headlineMedium,
				color = MaterialTheme.colorScheme.onBackground
			)
		}
	}
}
