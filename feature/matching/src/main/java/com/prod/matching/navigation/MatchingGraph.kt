package com.prod.matching.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.EntryProviderScope
import com.prod.match.navigation.MatchingNavKey
import com.prod.matching.screens.MainMatchingScreen
import org.koin.androidx.compose.koinViewModel

fun EntryProviderScope<Any>.matchingGraph(
	onUnauthorized: () -> Unit,
	onRequestNoInternet: (retryAction: () -> Unit) -> Unit,
	onOpenProfile: (userId: String) -> Unit
) {
	entry<MatchingNavKey> {
		MatchingScreen(
			onUnauthorized = onUnauthorized,
			onRequestNoInternet = onRequestNoInternet,
			onOpenProfile = onOpenProfile
		)
	}
}

@Composable
private fun MatchingScreen(
	onUnauthorized: () -> Unit,
	onRequestNoInternet: (retryAction: () -> Unit) -> Unit,
	onOpenProfile: (userId: String) -> Unit
) {
	Box(modifier = Modifier) {
		MainMatchingScreen(
			onUnauthorized = onUnauthorized,
			onRequestNoInternet = onRequestNoInternet,
			onOpenProfile = onOpenProfile,
			viewModel = koinViewModel()
		)
	}
}
