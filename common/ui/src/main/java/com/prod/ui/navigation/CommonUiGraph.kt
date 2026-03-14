package com.prod.ui.navigation

import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.navigation3.runtime.EntryProviderScope
import com.prod.ui.presentation.network.NoInternetScreen

fun EntryProviderScope<Any>.commonUiGraph(
	backStack: SnapshotStateList<Any>,
	consumeRetryAction: () -> (() -> Unit)?
) {
	entry<NoInternetNavKey> {
		NoInternetScreen(
			onRetry = {
				backStack.removeLastOrNull()
				consumeRetryAction()?.invoke()
			}
		)
	}
}
