package com.prod.t_match

import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.prod.auth.navigation.OnboardingNavKey
import com.prod.auth.navigation.rememberAuthGraphState
import com.prod.auth.navigation.authGraph
import com.prod.ui.navigation.commonUiGraph
import com.prod.ui.theme.TmatchTheme

@Composable
fun TMatchApp() {
	TmatchTheme {
		val backStack = rememberNavBackStack(OnboardingNavKey())
		val authGraphState = rememberAuthGraphState(backStack)
		val saveableStateHolder = rememberSaveableStateHolder()
		val viewModelStoreOwner = checkNotNull(LocalViewModelStoreOwner.current)

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
				authGraph(
					backStack = backStack,
					state = authGraphState
				)
				commonUiGraph(
					backStack = backStack,
					consumeRetryAction = authGraphState::consumeRetryAction
				)
			}
		)
	}
}
