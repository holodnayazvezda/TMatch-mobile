package com.prod.matching.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.EntryProviderScope
import com.prod.match.navigation.MatchingNavKey
import com.prod.matching.screens.MainMatchingScreen
import org.koin.androidx.compose.koinViewModel

fun EntryProviderScope<Any>.matchingGraph() {
	entry<MatchingNavKey> {
		MatchingScreen()
	}
}

@Composable
private fun MatchingScreen() {
	Box(modifier = Modifier) {
		MainMatchingScreen(
			viewModel = koinViewModel()
		)
	}
}
