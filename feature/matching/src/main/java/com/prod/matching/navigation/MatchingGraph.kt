package com.prod.matching.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.EntryProviderScope
import com.prod.match.navigation.MatchingNavKey
import com.prod.matching.screens.MainMatchingScreen
import org.koin.androidx.compose.koinViewModel

fun EntryProviderScope<Any>.matchingGraph(
	onUnauthorized: () -> Unit
) {
	entry<MatchingNavKey> {
		MatchingScreen(onUnauthorized = onUnauthorized)
	}
}

@Composable
private fun MatchingScreen(
	onUnauthorized: () -> Unit
) {
	Box(modifier = Modifier) {
		MainMatchingScreen(
			onUnauthorized = onUnauthorized,
			viewModel = koinViewModel()
		)
	}
}
