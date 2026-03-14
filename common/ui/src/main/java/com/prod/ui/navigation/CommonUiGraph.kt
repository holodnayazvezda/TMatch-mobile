package com.prod.ui.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.prod.ui.presentation.blank.BlankScreen
import com.prod.ui.presentation.network.NoInternetScreen

fun EntryProviderScope<Any>.commonUiGraph(
	backStack: SnapshotStateList<Any>,
	consumeRetryAction: () -> (() -> Unit)?
) {
	entry<BlankNavKey> {
		BlankScreen()
	}

	entry<NoInternetNavKey> {
		NoInternetScreen(
			onRetry = {
				backStack.removeLastOrNull()
				consumeRetryAction()?.invoke()
			}
		)
	}
}
