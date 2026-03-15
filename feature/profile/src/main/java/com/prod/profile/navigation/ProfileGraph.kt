package com.prod.profile.navigation

import androidx.navigation3.runtime.EntryProviderScope
import com.prod.profile.presentation.ProfileScreen

fun EntryProviderScope<Any>.profileGraph(
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
}
