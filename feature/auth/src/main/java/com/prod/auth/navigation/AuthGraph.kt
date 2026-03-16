package com.prod.auth.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.prod.auth.presentation.onboarding.OnboardingScreen
import com.prod.auth.presentation.profile.ProfileSetupScreen
import com.prod.auth.presentation.signin.SignInScreen
import com.prod.auth.presentation.signup.SignUpScreen
import kotlinx.coroutines.launch

fun EntryProviderScope<Any>.authGraph(
	backStack: SnapshotStateList<Any>,
	state: AuthGraphState
) {
	entry<OnboardingNavKey> { key ->
		OnboardingScreen(
			initialPage = key.initialPage,
			onCreateAccount = { backStack.add(SignUpNavKey()) },
			onLogin = { backStack.add(SignInNavKey()) }
		)
	}

	entry<SignInNavKey> { key ->
		SignInScreen(
			prefilledEmail = key.prefilledEmail,
			onBack = {
				if (backStack.size > 1) {
					backStack.removeLastOrNull()
				}
			},
			onSwitchToSignUp = { email ->
				backStack.replaceTop(SignUpNavKey(prefilledEmail = email.trim()))
			},
			onAuthSuccess = state::onSignInSuccess,
			onRequestNoInternet = state::onRequestNoInternet
		)
	}

	entry<SignUpNavKey> { key ->
		SignUpScreen(
			prefilledEmail = key.prefilledEmail,
			onBack = {
				if (backStack.size > 1) {
					backStack.removeLastOrNull()
				}
			},
			onSwitchToSignIn = { email ->
				backStack.replaceTop(SignInNavKey(prefilledEmail = email.trim()))
			},
			onAuthSuccess = state::onSignUpSuccess,
			onRequestNoInternet = state::onRequestNoInternet
		)
	}

	entry<ProfileSetupNavKey> {
		val coroutineScope = rememberCoroutineScope()
		ProfileSetupScreen(
			onProfileCompleted = {
				state.onProfileCompleted()
			},
			onRequestNoInternet = state::onRequestNoInternet,
			onUnauthorized = {
				coroutineScope.launch {
					state.onUnauthorized()
				}
			}
		)
	}
}

private fun SnapshotStateList<Any>.replaceTop(destination: Any) {
	if (isEmpty()) {
		add(destination)
		return
	}

	this[lastIndex] = destination
}
