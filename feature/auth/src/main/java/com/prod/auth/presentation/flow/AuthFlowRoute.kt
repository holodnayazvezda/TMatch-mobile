package com.prod.auth.presentation.flow

import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.prod.auth.domain.model.AuthSession
import com.prod.auth.presentation.flow.model.AuthNavKey
import com.prod.auth.presentation.flow.model.OnboardingNavKey
import com.prod.auth.presentation.flow.model.SignInNavKey
import com.prod.auth.presentation.flow.model.SignUpNavKey
import com.prod.auth.presentation.onboarding.OnboardingScreen
import com.prod.auth.presentation.signin.SignInScreen
import com.prod.auth.presentation.signup.SignUpScreen

@Composable
fun AuthFlowRoute(
	initialDestination: AuthNavKey,
	onAuthSuccess: (AuthSession) -> Unit,
	onRequestNoInternet: (retryAction: () -> Unit) -> Unit,
	modifier: Modifier = Modifier
) {
	val backStack = rememberNavBackStack(initialDestination)
	val saveableStateHolder = rememberSaveableStateHolder()
	val viewModelStoreOwner = checkNotNull(LocalViewModelStoreOwner.current)

	NavDisplay(
		backStack = backStack,
		modifier = modifier,
		entryDecorators = listOf(
			rememberSaveableStateHolderNavEntryDecorator(saveableStateHolder),
			rememberViewModelStoreNavEntryDecorator(viewModelStoreOwner) { true }
		),
		onBack = {
			if (backStack.size > 1) {
				backStack.removeLastOrNull()
			}
		},
		entryProvider = entryProvider {
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
					onAuthSuccess = onAuthSuccess,
					onRequestNoInternet = onRequestNoInternet
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
					onAuthSuccess = onAuthSuccess,
					onRequestNoInternet = onRequestNoInternet
				)
			}
		}
	)
}

private fun NavBackStack<NavKey>.replaceTop(destination: AuthNavKey) {
	if (isEmpty()) {
		add(destination)
		return
	}

	this[lastIndex] = destination
}
