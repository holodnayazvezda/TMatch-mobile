package com.prod.t_match

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import com.prod.auth.presentation.flow.AuthFlowRoute
import com.prod.t_match.navigation.model.AuthFlowNavKey
import com.prod.t_match.navigation.model.BlankNavKey
import com.prod.t_match.navigation.model.NoInternetNavKey
import com.prod.t_match.presentation.blank.BlankScreen
import com.prod.t_match.presentation.network.NoInternetScreen
import com.prod.ui.theme.TmatchTheme
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay

@Composable
fun TMatchApp() {
	TmatchTheme {
		val backStack = rememberNavBackStack(AuthFlowNavKey())
		var nextRetryRequestId by rememberSaveable { mutableStateOf(0L) }
		var retryAction by remember { mutableStateOf<(() -> Unit)?>(null) }
		val saveableStateHolder = rememberSaveableStateHolder()
		val viewModelStoreOwner = checkNotNull(LocalViewModelStoreOwner.current)

		LaunchedEffect(backStack.lastOrNull()) {
			if (backStack.lastOrNull() !is NoInternetNavKey) {
				retryAction = null
			}
		}

		NavDisplay(
			backStack = backStack,
			modifier = Modifier,
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
				entry<AuthFlowNavKey> { key ->
					AuthFlowRoute(
						initialDestination = key.initialDestination,
						onAuthSuccess = {
							backStack.clear()
							backStack.add(BlankNavKey(sessionStartedAtMillis = System.currentTimeMillis()))
						},
						onRequestNoInternet = { callback ->
							// Колбэк не живёт в back stack, поэтому держим его рядом с route по requestId.
							retryAction = callback
							nextRetryRequestId += 1L
							backStack.showNoInternet(NoInternetNavKey(requestId = nextRetryRequestId))
						}
					)
				}

				entry<BlankNavKey> {
					BlankScreen()
				}

				entry<NoInternetNavKey> {
					NoInternetScreen(
						onRetry = {
							backStack.removeLastOrNull()
							val callback = retryAction
							retryAction = null
							callback?.invoke()
						}
					)
				}
			}
		)
	}
}

private fun NavBackStack<NavKey>.showNoInternet(destination: NoInternetNavKey) {
	if (lastOrNull() is NoInternetNavKey) {
		this[lastIndex] = destination
		return
	}

	add(destination)
}
