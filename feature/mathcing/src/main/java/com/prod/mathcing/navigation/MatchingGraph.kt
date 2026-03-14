package com.prod.mathcing.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.prod.match.navigation.MatchingNavKey
import com.prod.ui.components.AppGradientBackground

fun EntryProviderScope<NavKey>.matchingGraph() {
	entry<MatchingNavKey> {
		MatchingScreen()
	}
}

@Composable
private fun MatchingScreen() {
	AppGradientBackground {
		Box(
			modifier = Modifier.fillMaxSize(),
			contentAlignment = Alignment.Center
		) {
			Text(
				text = "Мэтчинг",
				style = MaterialTheme.typography.headlineMedium,
				color = MaterialTheme.colorScheme.onBackground
			)
		}
	}
}
