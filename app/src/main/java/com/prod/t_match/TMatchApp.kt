package com.prod.t_match

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import androidx.annotation.StringRes
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.prod.auth.domain.gateway.AuthSessionStore
import com.prod.common.api.gateway.NotificationGateway
import com.prod.auth.navigation.authGraph
import com.prod.auth.navigation.rememberAuthGraphState
import com.prod.chats.navigation.ChatsNavKey
import com.prod.chats.navigation.chatsGraph
import com.prod.common.api.model.UserProfileFeedAction
import com.prod.match.navigation.MatchingNavKey
import com.prod.matching.domain.gateway.PendingMatchActionStore
import com.prod.matching.navigation.matchingGraph
import com.prod.profile.domain.gateway.ProfileGateway
import com.prod.profile.navigation.ProfileNavKey
import com.prod.profile.navigation.UserProfileNavKey
import com.prod.profile.navigation.profileGraph
import com.prod.t_match.debug.DebugNetworkOverridesNavKey
import com.prod.t_match.debug.DebugNetworkOverridesScreen
import com.prod.t_match.debug.DebugShakeToOpenEffect
import com.prod.t_match.navigation.AppStartDestinationResolver
import com.prod.t_match.notifications.FcmTokenRegistrar
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
		val fcmTokenRegistrar = remember(authSessionStore, notificationGateway) {
			FcmTokenRegistrar(
				authSessionStore = authSessionStore,
				notificationGateway = notificationGateway
			)
		}
		val startDestination by produceState<Any?>(
			initialValue = null,
			authSessionStore,
			profileGateway,
			pendingMatchActionStore
		) {
			value = AppStartDestinationResolver(
				authSessionStore = authSessionStore,
				profileGateway = profileGateway,
				pendingMatchActionStore = pendingMatchActionStore
			).resolve()
		}
		val backgroundBrush = rememberAppGradientBrush()
		if (startDestination == null) {
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
		val resolvedStartDestination = requireNotNull(startDestination)
		val backStack = remember(resolvedStartDestination) {
			mutableStateListOf<Any>(resolvedStartDestination)
		}
		var hasDatingProfileAccess by remember(resolvedStartDestination) {
			mutableStateOf(authSessionStore.hasDatingProfile())
		}
		val authGraphState = rememberAuthGraphState(
			backStack = backStack,
			authSessionStore = authSessionStore,
			onSessionCleared = pendingMatchActionStore::clear,
			onSessionStarted = fcmTokenRegistrar::syncCurrentTokenIfAuthorized,
			resolveSignedInDestination = {
				AppStartDestinationResolver(
					authSessionStore = authSessionStore,
					profileGateway = profileGateway,
					pendingMatchActionStore = pendingMatchActionStore
				).resolveAuthenticatedDestination()
			}
		)
		val saveableStateHolder = rememberSaveableStateHolder()
		val viewModelStoreOwner = checkNotNull(LocalViewModelStoreOwner.current)
		val currentDestination = backStack.lastOrNull()
		var userProfileFeedActionHandler by remember {
			mutableStateOf<((userId: String, action: UserProfileFeedAction) -> Unit)?>(null)
		}

		LaunchedEffect(Unit) {
			fcmTokenRegistrar.syncCurrentTokenIfAuthorized()
		}

		LaunchedEffect(currentDestination) {
			hasDatingProfileAccess = authSessionStore.hasDatingProfile()
		}

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
			contentWindowInsets = WindowInsets(0, 0, 0, 0),
			bottomBar = {
				if (hasDatingProfileAccess && currentDestination.isMainTabDestination()) {
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
				modifier = Modifier
					.padding(innerPadding)
					.consumeWindowInsets(innerPadding),
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
						onBack = {
							if (backStack.size > 1) {
								backStack.removeLastOrNull()
							}
						},
						onFeedAction = { userId, action ->
							userProfileFeedActionHandler?.invoke(userId, action)
							if (backStack.size > 1) {
								backStack.removeLastOrNull()
							}
						},
						onDatingProfileAvailabilityChanged = { hasDatingProfile ->
							hasDatingProfileAccess = hasDatingProfile
							authSessionStore.setHasDatingProfile(hasDatingProfile)
						},
						onUnauthorized = authGraphState::onUnauthorized,
						onProfileMissing = authGraphState::onProfileMissing,
						onLogout = authGraphState::onLogout,
						onRequestNoInternet = authGraphState::onRequestNoInternet
					)
					matchingGraph(
						onUnauthorized = authGraphState::onUnauthorized,
						onRequestNoInternet = authGraphState::onRequestNoInternet,
						onOpenProfile = { profile ->
							backStack.add(
								UserProfileNavKey(
									userId = profile.id,
									source = "matching_card",
									reasons = profile.reasons,
									recommendationId = profile.recommendationId
								)
							)
						},
						onProfileActionHandlerReady = { handler ->
							userProfileFeedActionHandler = handler
						}
					)
					chatsGraph(
						onUnauthorized = authGraphState::onUnauthorized,
						onRequestNoInternet = authGraphState::onRequestNoInternet
					)
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
				icon = { Text(stringResource(destination.shortLabelRes)) },
				label = { Text(stringResource(destination.titleRes)) }
			)
		}
	}
}

private fun Any?.isMainTabDestination(): Boolean {
	return this is ProfileNavKey || this is MatchingNavKey || this is ChatsNavKey
}

private enum class MainTabDestination(
	@param:StringRes val titleRes: Int,
	@param:StringRes val shortLabelRes: Int,
	val key: Any
) {
	Profile(
		titleRes = R.string.main_tab_profile,
		shortLabelRes = R.string.main_tab_profile_short,
		key = ProfileNavKey()
	),
	Matching(
		titleRes = R.string.main_tab_matching,
		shortLabelRes = R.string.main_tab_matching_short,
		key = MatchingNavKey()
	),
	Chats(
		titleRes = R.string.main_tab_chats,
		shortLabelRes = R.string.main_tab_chats_short,
		key = ChatsNavKey()
	)
}
