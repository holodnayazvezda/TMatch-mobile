package com.prod.profile.navigation

import androidx.navigation3.runtime.EntryProviderScope
import com.prod.profile.presentation.ProfileScreen

fun EntryProviderScope<Any>.profileGraph(
	onUnauthorized: () -> Unit,
	onLogout: () -> Unit,
	onRequestNoInternet: (retryAction: () -> Unit) -> Unit,
	onOpenDebugTools: (() -> Unit)? = null
) {
	entry<ProfileNavKey> {
		ProfileScreen(
			onUnauthorized = onUnauthorized,
			onLogout = onLogout,
			onRequestNoInternet = onRequestNoInternet,
			onOpenDebugTools = onOpenDebugTools
		)
	}
}
