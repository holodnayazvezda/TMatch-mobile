package com.prod.t_match

import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.consumeWindowInsets
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
import com.prod.auth.navigation.OnboardingNavKey
import com.prod.auth.navigation.authGraph
import com.prod.auth.navigation.rememberAuthGraphState
import com.prod.chats.navigation.ChatsNavKey
import com.prod.chats.navigation.chatsGraph
import com.prod.match.navigation.MatchingNavKey
import com.prod.mathcing.navigation.matchingGraph
import com.prod.profile.navigation.ProfileNavKey
import com.prod.profile.navigation.profileGraph
import com.prod.ui.navigation.commonUiGraph
import com.prod.ui.theme.TmatchTheme

@Composable
fun TMatchApp() {
	TmatchTheme {
		val backStack = remember { mutableStateListOf<Any>(OnboardingNavKey()) }
		val authGraphState = rememberAuthGraphState(backStack)
		val saveableStateHolder = rememberSaveableStateHolder()
		val viewModelStoreOwner = checkNotNull(LocalViewModelStoreOwner.current)
		val currentDestination = backStack.lastOrNull()

		Scaffold(
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
		) { innerPadding ->
			innerPadding
			NavDisplay(
				backStack = backStack,
				modifier = Modifier.padding(),
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
					profileGraph()
					matchingGraph()
					chatsGraph()
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
