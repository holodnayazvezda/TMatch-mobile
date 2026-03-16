package com.prod.profile.navigation

import androidx.navigation3.runtime.EntryProviderScope
import com.prod.profile.presentation.ProfileScreen
import com.prod.profile.presentation.UserProfileScreen

fun EntryProviderScope<Any>.profileGraph(
	onBack: () -> Unit,
	onUnauthorized: () -> Unit,
	onProfileMissing: () -> Unit,
	onLogout: () -> Unit,
	onRequestNoInternet: (retryAction: () -> Unit) -> Unit
) {
	entry<ProfileNavKey> {
		ProfileScreen(
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
			onUnauthorized = onUnauthorized,
			onRequestNoInternet = onRequestNoInternet
		)
	}
}
