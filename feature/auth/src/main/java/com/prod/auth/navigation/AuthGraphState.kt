package com.prod.auth.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.prod.profile.navigation.ProfileNavKey
import com.prod.ui.navigation.NoInternetNavKey

class AuthGraphState(
	private val backStack: SnapshotStateList<Any>,
	private val consumeRetryActionInternal: () -> (() -> Unit)?,
	private val requestNoInternetInternal: (retryAction: () -> Unit) -> Unit
) {
	fun onAuthSuccess() {
		backStack.clear()
		backStack.add(ProfileNavKey())
	}

	fun onRequestNoInternet(retryAction: () -> Unit) = requestNoInternetInternal(retryAction)

	fun consumeRetryAction(): (() -> Unit)? = consumeRetryActionInternal()
}

@Composable
fun rememberAuthGraphState(
	backStack: SnapshotStateList<Any>
): AuthGraphState {
	var nextRetryRequestId by rememberSaveable { mutableStateOf(0L) }
	var retryAction by remember { mutableStateOf<(() -> Unit)?>(null) }

	LaunchedEffect(backStack.lastOrNull()) {
		if (backStack.lastOrNull() !is NoInternetNavKey) {
			retryAction = null
		}
	}

	return AuthGraphState(
		backStack = backStack,
		consumeRetryActionInternal = {
			val callback = retryAction
			retryAction = null
			callback
		},
		requestNoInternetInternal = { callback ->
			retryAction = callback
			nextRetryRequestId += 1L
			backStack.showNoInternet(NoInternetNavKey(requestId = nextRetryRequestId))
		}
	)
}

private fun SnapshotStateList<Any>.showNoInternet(destination: NoInternetNavKey) {
	if (lastOrNull() is NoInternetNavKey) {
		this[lastIndex] = destination
		return
	}

	add(destination)
}
