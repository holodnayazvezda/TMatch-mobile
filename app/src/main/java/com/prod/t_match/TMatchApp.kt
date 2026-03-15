package com.prod.t_match

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.prod.auth.domain.gateway.AuthSessionStore
import com.prod.auth.navigation.OnboardingNavKey
import com.prod.auth.navigation.ProfileSetupNavKey
import com.prod.auth.navigation.authGraph
import com.prod.auth.navigation.rememberAuthGraphState
import com.prod.chats.navigation.ChatsNavKey
import com.prod.chats.navigation.chatsGraph
import com.prod.match.navigation.MatchingNavKey
import com.prod.matching.navigation.matchingGraph
import com.prod.profile.navigation.ProfileNavKey
import com.prod.profile.navigation.profileGraph
import com.prod.t_match.debug.DebugNetworkOverridesNavKey
import com.prod.t_match.debug.DebugNetworkOverridesScreen
import com.prod.t_match.debug.DebugShakeToOpenEffect
import com.prod.ui.components.rememberAppGradientBrush
import com.prod.ui.navigation.commonUiGraph
import com.prod.ui.theme.TmatchTheme
import org.koin.compose.koinInject

@Composable
fun TMatchApp() {
	TmatchTheme {
		val authSessionStore = koinInject<AuthSessionStore>()
		val startDestination = remember(authSessionStore) {
			if (authSessionStore.get() == null) {
				OnboardingNavKey()
			} else if (authSessionStore.isProfileCompleted()) {
				ProfileNavKey()
			} else {
				ProfileSetupNavKey()
			}
		}
		val backStack = remember(startDestination) { mutableStateListOf<Any>(startDestination) }
		val authGraphState = rememberAuthGraphState(
			backStack = backStack,
			authSessionStore = authSessionStore
		)
		val backgroundBrush = rememberAppGradientBrush()
		val saveableStateHolder = rememberSaveableStateHolder()
		val viewModelStoreOwner = checkNotNull(LocalViewModelStoreOwner.current)
		val currentDestination = backStack.lastOrNull()

		if (BuildConfig.DEBUG) {
			DebugShakeToOpenEffect(
				onShake = {
					if (backStack.lastOrNull() !is DebugNetworkOverridesNavKey) {
						backStack.add(DebugNetworkOverridesNavKey(source = "shake"))
					}
				}
			)
		}

		Scaffold(
			modifier = Modifier
				.fillMaxSize()
				.background(backgroundBrush),
			bottomBar = {
				if (currentDestination.isMainTabDestination()) {
					MainBottomBar(
						currentDestination = currentDestination,
						onTabSelected = { destination ->
							if (currentDestination?.let { it::class } == destination::class) {
								return@MainBottomBar
							}
							backStack.clear()
							backStack.add(destination)
						}
					)
				}
			},
			containerColor = androidx.compose.ui.graphics.Color.Transparent
		) { innerPadding ->
			NavDisplay(
				backStack = backStack,
				modifier = Modifier.padding(innerPadding),
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
					profileGraph(
						onUnauthorized = authGraphState::onUnauthorized,
						onRequestNoInternet = authGraphState::onRequestNoInternet,
						onOpenDebugTools = if (BuildConfig.DEBUG) {
							{
								backStack.add(DebugNetworkOverridesNavKey())
							}
						} else {
							null
						}
					)
					matchingGraph(
						onUnauthorized = authGraphState::onUnauthorized
					)
					chatsGraph()
					entry<DebugNetworkOverridesNavKey> {
						DebugNetworkOverridesScreen(
							onBack = {
								if (backStack.size > 1) {
									backStack.removeLastOrNull()
								}
							}
						)
					}
					commonUiGraph(
						backStack = backStack,
						consumeRetryAction = authGraphState::consumeRetryAction
					)
				}
			)
		}
	}
}

@Composable
private fun MainBottomBar(
	currentDestination: Any?,
	onTabSelected: (Any) -> Unit
) {
	NavigationBar {
		MainTabDestination.entries.forEach { destination ->
			NavigationBarItem(
				selected = currentDestination?.let { it::class } == destination.key::class,
				onClick = { onTabSelected(destination.key) },
				icon = { Text(destination.shortLabel) },
				label = { Text(destination.title) }
			)
		}
	}
}

private fun Any?.isMainTabDestination(): Boolean {
	return this is ProfileNavKey || this is MatchingNavKey || this is ChatsNavKey
}

private enum class MainTabDestination(
	val title: String,
	val shortLabel: String,
	val key: Any
) {
	Profile(
		title = "Профиль",
		shortLabel = "П",
		key = ProfileNavKey()
	),
	Matching(
		title = "Мэтчинг",
		shortLabel = "М",
		key = MatchingNavKey()
	),
	Chats(
		title = "Чаты",
		shortLabel = "Ч",
		key = ChatsNavKey()
	)
}
