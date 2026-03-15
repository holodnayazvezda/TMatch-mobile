package com.prod.auth.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.prod.auth.domain.gateway.AuthSessionStore
import com.prod.common.api.model.UserSession
import com.prod.profile.navigation.ProfileNavKey
import com.prod.ui.navigation.NoInternetNavKey

class AuthGraphState(
	private val backStack: SnapshotStateList<Any>,
	private val authSessionStore: AuthSessionStore,
	private val consumeRetryActionInternal: () -> (() -> Unit)?,
	private val requestNoInternetInternal: (retryAction: () -> Unit) -> Unit
) {
	fun onSignInSuccess(session: UserSession) {
		authSessionStore.save(session)
		authSessionStore.setProfileCompleted(false)
		backStack.clear()
		backStack.add(ProfileSetupNavKey())
	}

	fun onSignUpSuccess(session: UserSession) {
		authSessionStore.save(session)
		authSessionStore.setProfileCompleted(false)
		backStack.clear()
		backStack.add(ProfileSetupNavKey())
	}

	fun onProfileCompleted() {
		authSessionStore.setProfileCompleted(true)
		backStack.clear()
		backStack.add(ProfileNavKey())
	}

	fun onProfileMissing() {
		authSessionStore.setProfileCompleted(false)
		backStack.clear()
		backStack.add(ProfileSetupNavKey())
	}

	fun onUnauthorized() {
		authSessionStore.clear()
		backStack.clear()
		backStack.add(OnboardingNavKey())
	}

	fun onLogout() {
		authSessionStore.clear()
		backStack.clear()
		backStack.add(OnboardingNavKey())
		backStack.add(SignInNavKey())
	}

	fun onRequestNoInternet(retryAction: () -> Unit) = requestNoInternetInternal(retryAction)

	fun consumeRetryAction(): (() -> Unit)? = consumeRetryActionInternal()
}

@Composable
fun rememberAuthGraphState(
	backStack: SnapshotStateList<Any>,
	authSessionStore: AuthSessionStore
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
		authSessionStore = authSessionStore,
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
