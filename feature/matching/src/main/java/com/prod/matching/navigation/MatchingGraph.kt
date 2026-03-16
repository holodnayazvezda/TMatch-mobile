package com.prod.matching.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.EntryProviderScope
import com.prod.common.api.model.UserProfileFeedAction
import com.prod.match.navigation.MatchingNavKey
import com.prod.matching.model.MatchingDeckItem.Profile
import com.prod.matching.screens.MainMatchingScreen
import org.koin.androidx.compose.koinViewModel

fun EntryProviderScope<Any>.matchingGraph(
	onUnauthorized: () -> Unit,
	onRequestNoInternet: (retryAction: () -> Unit) -> Unit,
	onOpenProfile: (profile: Profile) -> Unit,
	onProfileActionHandlerReady: ((userId: String, action: UserProfileFeedAction) -> Unit) -> Unit
) {
	entry<MatchingNavKey> {
		MatchingScreen(
			onUnauthorized = onUnauthorized,
			onRequestNoInternet = onRequestNoInternet,
			onOpenProfile = onOpenProfile,
			onProfileActionHandlerReady = onProfileActionHandlerReady
		)
	}
}

@Composable
private fun MatchingScreen(
	onUnauthorized: () -> Unit,
	onRequestNoInternet: (retryAction: () -> Unit) -> Unit,
	onOpenProfile: (profile: Profile) -> Unit,
	onProfileActionHandlerReady: ((userId: String, action: UserProfileFeedAction) -> Unit) -> Unit
) {
	Box(modifier = Modifier) {
		MainMatchingScreen(
			onUnauthorized = onUnauthorized,
			onRequestNoInternet = onRequestNoInternet,
			onOpenProfile = onOpenProfile,
			onProfileActionHandlerReady = onProfileActionHandlerReady,
			viewModel = koinViewModel()
		)
	}
}
