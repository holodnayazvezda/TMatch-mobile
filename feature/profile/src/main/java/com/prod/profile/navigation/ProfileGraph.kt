package com.prod.profile.navigation

import androidx.navigation3.runtime.EntryProviderScope
import com.prod.common.api.model.UserProfileFeedAction
import com.prod.profile.presentation.ProfileScreen
import com.prod.profile.presentation.UserProfileScreen

fun EntryProviderScope<Any>.profileGraph(
	onBack: () -> Unit,
	onFeedAction: (userId: String, action: UserProfileFeedAction) -> Unit,
	onOpenChat: (userId: String) -> Unit,
	onDatingProfileAvailabilityChanged: (Boolean) -> Unit,
	onUnauthorized: () -> Unit,
	onProfileMissing: () -> Unit,
	onLogout: () -> Unit,
	onRequestNoInternet: (retryAction: () -> Unit) -> Unit
) {
	entry<ProfileNavKey> {
		ProfileScreen(
			onDatingProfileAvailabilityChanged = onDatingProfileAvailabilityChanged,
			onUnauthorized = onUnauthorized,
			onProfileMissing = onProfileMissing,
			onLogout = onLogout,
			onRequestNoInternet = onRequestNoInternet
		)
	}
	entry<UserProfileNavKey> { key ->
		UserProfileScreen(
			userId = key.userId,
			onBack = onBack,
			reasons = key.reasons,
			showFeedActions = key.source == "matching_card",
			onLike = { onFeedAction(key.userId, UserProfileFeedAction.Like) },
			onDislike = { onFeedAction(key.userId, UserProfileFeedAction.Dislike) },
			onOpenChat = { onOpenChat(key.userId) },
			onBan = { onFeedAction(key.userId, UserProfileFeedAction.Ban) },
			onUnauthorized = onUnauthorized,
			onRequestNoInternet = onRequestNoInternet
		)
	}
}
