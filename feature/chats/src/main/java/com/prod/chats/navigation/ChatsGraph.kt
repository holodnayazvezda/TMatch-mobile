package com.prod.chats.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.EntryProviderScope
import com.prod.ui.components.AppScreenTopBar

fun EntryProviderScope<Any>.chatsGraph() {
	entry<ChatsNavKey> {
		ChatsScreen()
	}
}

@Composable
private fun ChatsScreen() {
	Column(
		modifier = Modifier
			.fillMaxSize()
			.safeDrawingPadding()
	) {
		AppScreenTopBar(
			title = "Чаты",
			applySafeDrawingPadding = false
		)
		Box(
			modifier = Modifier
				.fillMaxWidth()
				.weight(1f),
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
