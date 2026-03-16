package com.prod.t_match

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.prod.auth.domain.gateway.AuthSessionStore
import com.prod.common.api.gateway.NotificationGateway
import com.prod.auth.navigation.authGraph
import com.prod.chats.navigation.ChatsNavKey
import com.prod.chats.navigation.chatsGraph
import com.prod.match.navigation.MatchingNavKey
import com.prod.matching.domain.gateway.PendingMatchActionStore
import com.prod.matching.navigation.matchingGraph
import com.prod.profile.domain.gateway.ProfileGateway
import com.prod.profile.navigation.ProfileNavKey
import com.prod.profile.navigation.profileGraph
import com.prod.t_match.debug.DebugNetworkOverridesNavKey
import com.prod.t_match.debug.DebugNetworkOverridesScreen
import com.prod.t_match.debug.DebugAppLogsNavKey
import com.prod.t_match.debug.DebugAppLogsScreen
import com.prod.t_match.debug.DebugShakeToOpenEffect
import com.prod.t_match.navigation.isMainTabDestination
import com.prod.t_match.navigation.rememberTMatchAppState
import com.prod.ui.components.rememberAppGradientBrush
import com.prod.ui.navigation.commonUiGraph
import com.prod.ui.theme.TmatchTheme
import org.koin.compose.koinInject

@Composable
fun TMatchApp() {
	TmatchTheme {
		val authSessionStore = koinInject<AuthSessionStore>()
		val profileGateway = koinInject<ProfileGateway>()
		val notificationGateway = koinInject<NotificationGateway>()
		val pendingMatchActionStore = koinInject<PendingMatchActionStore>()
		val appState = rememberTMatchAppState(
			authSessionStore = authSessionStore,
			profileGateway = profileGateway,
			notificationGateway = notificationGateway,
			pendingMatchActionStore = pendingMatchActionStore
		)
		val backgroundBrush = rememberAppGradientBrush()
		if (appState == null) {
			Box(
				modifier = Modifier
					.fillMaxSize()
					.background(backgroundBrush),
				contentAlignment = Alignment.Center
			) {
				CircularProgressIndicator()
			}
			return@TmatchTheme
		}
		val authGraphState = appState.rememberAuthGraphState()
		val saveableStateHolder = rememberSaveableStateHolder()
		val viewModelStoreOwner = checkNotNull(LocalViewModelStoreOwner.current)
		val currentDestination = appState.currentDestination

		if (BuildConfig.DEBUG) {
			DebugShakeToOpenEffect(
				onShake = {
					if (appState.backStack.lastOrNull() !is DebugNetworkOverridesNavKey) {
						appState.backStack.add(DebugNetworkOverridesNavKey(source = "shake"))
					}
				}
			)
		}

		Scaffold(
			modifier = Modifier
				.fillMaxSize()
				.background(backgroundBrush),
			contentWindowInsets = WindowInsets(0, 0, 0, 0),
			bottomBar = {
				if (appState.hasDatingProfileAccess && currentDestination.isMainTabDestination()) {
					MainBottomBar(
						currentDestination = currentDestination,
						onTabSelected = appState::onMainTabSelected
					)
				}
			},
			containerColor = androidx.compose.ui.graphics.Color.Transparent
		) { innerPadding ->
			NavDisplay(
				backStack = appState.backStack,
				modifier = Modifier
					.padding(innerPadding)
					.consumeWindowInsets(innerPadding),
				entryDecorators = listOf(
					rememberSaveableStateHolderNavEntryDecorator(saveableStateHolder),
					rememberViewModelStoreNavEntryDecorator(viewModelStoreOwner) { true }
				),
				onBack = appState::onBack,
				entryProvider = entryProvider {
					authGraph(
						backStack = appState.backStack,
						state = authGraphState
					)
					profileGraph(
						onBack = appState::onBack,
						onFeedAction = appState::onFeedAction,
						onOpenChat = { appState.onOpenChatFromProfile() },
						onDatingProfileAvailabilityChanged = appState::onDatingProfileAvailabilityChanged,
						onUnauthorized = authGraphState::onUnauthorized,
						onProfileMissing = authGraphState::onProfileMissing,
						onLogout = authGraphState::onLogout,
						onRequestNoInternet = authGraphState::onRequestNoInternet
					)
					matchingGraph(
						onUnauthorized = authGraphState::onUnauthorized,
						onRequestNoInternet = authGraphState::onRequestNoInternet,
						onOpenProfile = { profile ->
							appState.onOpenUserProfile(
								userId = profile.id,
								reasons = profile.reasons,
								recommendationId = profile.recommendationId
							)
						},
						onProfileActionHandlerReady = appState::registerUserProfileFeedActionHandler
					)
					chatsGraph(
						onUnauthorized = authGraphState::onUnauthorized,
						onRequestNoInternet = authGraphState::onRequestNoInternet
					)
					entry<DebugNetworkOverridesNavKey> {
						DebugNetworkOverridesScreen(
							onOpenLogs = {
								appState.backStack.add(DebugAppLogsNavKey())
							},
							onBack = appState::onBack
						)
					}
					entry<DebugAppLogsNavKey> {
						DebugAppLogsScreen(
							onBack = appState::onBack
						)
					}
					commonUiGraph(
						backStack = appState.backStack,
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
				icon = {
					Icon(
						painter = painterResource(destination.iconRes),
						contentDescription = stringResource(destination.titleRes)
					)
				},
				label = { Text(stringResource(destination.titleRes)) }
			)
		}
	}
}

private enum class MainTabDestination(
	@param:StringRes val titleRes: Int,
	@param:StringRes val shortLabelRes: Int,
	@param:DrawableRes val iconRes: Int,
	val key: Any
) {
	Profile(
		titleRes = R.string.main_tab_profile,
		shortLabelRes = R.string.main_tab_profile_short,
		iconRes = R.drawable.profile,
		key = ProfileNavKey()
	),
	Matching(
		titleRes = R.string.main_tab_matching,
		shortLabelRes = R.string.main_tab_matching_short,
		iconRes = R.drawable.match,
		key = MatchingNavKey()
	),
	Chats(
		titleRes = R.string.main_tab_chats,
		shortLabelRes = R.string.main_tab_chats_short,
		iconRes = R.drawable.chat,
		key = ChatsNavKey()
	)
}
