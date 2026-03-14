package com.prod.auth.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.prod.auth.presentation.onboarding.OnboardingScreen
import com.prod.auth.presentation.signin.SignInScreen
import com.prod.auth.presentation.signup.SignUpScreen

fun EntryProviderScope<NavKey>.authGraph(
	backStack: NavBackStack<NavKey>,
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
			onAuthSuccess = { state.onAuthSuccess() },
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
			onAuthSuccess = { state.onAuthSuccess() },
			onRequestNoInternet = state::onRequestNoInternet
		)
	}
}

private fun NavBackStack<NavKey>.replaceTop(destination: NavKey) {
	if (isEmpty()) {
		add(destination)
		return
	}

	this[lastIndex] = destination
}
