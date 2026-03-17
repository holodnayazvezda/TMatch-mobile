package com.prod.t_match.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.prod.auth.domain.gateway.AuthSessionStore
import com.prod.auth.navigation.rememberAuthGraphState
import com.prod.chats.domain.gateway.ChatGateway
import com.prod.chats.navigation.ChatsNavKey
import com.prod.common.api.gateway.NotificationGateway
import com.prod.common.api.model.UserProfileFeedAction
import com.prod.match.navigation.MatchingNavKey
import com.prod.matching.domain.gateway.PendingMatchActionStore
import com.prod.profile.domain.gateway.ProfileGateway
import com.prod.profile.navigation.ProfileNavKey
import com.prod.profile.navigation.UserProfileNavKey
import com.prod.t_match.notifications.FcmTokenRegistrar

class TMatchAppState(
	startDestination: Any,
	private val authSessionStore: AuthSessionStore,
	profileGateway: ProfileGateway,
	notificationGateway: NotificationGateway,
	chatGateway: ChatGateway,
	pendingMatchActionStore: PendingMatchActionStore
) {
	private data class PendingUserProfileFeedAction(
		val userId: String,
		val action: UserProfileFeedAction
	)

	val backStack: SnapshotStateList<Any> = mutableStateListOf(startDestination)

	var hasDatingProfileAccess by mutableStateOf(authSessionStore.hasDatingProfile())
		private set

	var userProfileFeedActionHandler by mutableStateOf<((userId: String, action: UserProfileFeedAction) -> Unit)?>(null)
		private set

	private var pendingUserProfileFeedAction by mutableStateOf<PendingUserProfileFeedAction?>(null)

	val currentDestination: Any?
		get() = backStack.lastOrNull()

	private val fcmTokenRegistrar = FcmTokenRegistrar(
		authSessionStore = authSessionStore,
		notificationGateway = notificationGateway
	)

	private val authGraphStateHolder = AuthGraphStateHolder(
		authSessionStore = authSessionStore,
		profileGateway = profileGateway,
		chatGateway = chatGateway,
		pendingMatchActionStore = pendingMatchActionStore,
		fcmTokenRegistrar = fcmTokenRegistrar,
		backStack = backStack
	)

	@Composable
	fun rememberAuthGraphState() = authGraphStateHolder.rememberState()

	suspend fun syncCurrentTokenIfAuthorized() {
		fcmTokenRegistrar.syncCurrentTokenIfAuthorized()
	}

	fun syncDatingProfileAccess() {
		hasDatingProfileAccess = authSessionStore.hasDatingProfile()
	}

	fun onMainTabSelected(destination: Any) {
		if (currentDestination?.let { it::class } == destination::class) {
			return
		}
		backStack.clear()
		backStack.add(destination)
	}

	fun onBack() {
		if (backStack.size > 1) {
			backStack.removeLastOrNull()
		}
	}

	fun onFeedAction(userId: String, action: UserProfileFeedAction) {
		pendingUserProfileFeedAction = PendingUserProfileFeedAction(
			userId = userId,
			action = action
		)
		onBack()
		dispatchPendingUserProfileFeedAction()
	}

	fun onOpenChatFromProfile(userId: String) {
		backStack.clear()
		backStack.add(ChatsNavKey(source = "user_profile_menu:$userId"))
	}

	fun onDatingProfileAvailabilityChanged(hasDatingProfile: Boolean) {
		hasDatingProfileAccess = hasDatingProfile
		authSessionStore.setHasDatingProfile(hasDatingProfile)
	}

	fun onOpenUserProfile(userId: String, reasons: List<com.prod.common.api.model.RecommendationReasonSummary>, recommendationId: String?) {
		backStack.add(
			UserProfileNavKey(
				userId = userId,
				source = "matching_card",
				reasons = reasons,
				recommendationId = recommendationId
			)
		)
	}

	fun onOpenUserProfileFromChat(userId: String) {
		backStack.add(
			UserProfileNavKey(
				userId = userId,
				source = "chat_header",
				reasons = emptyList(),
				recommendationId = null
			)
		)
	}

	fun registerUserProfileFeedActionHandler(handler: (userId: String, action: UserProfileFeedAction) -> Unit) {
		userProfileFeedActionHandler = handler
		dispatchPendingUserProfileFeedAction()
	}

	private fun dispatchPendingUserProfileFeedAction() {
		val pendingAction = pendingUserProfileFeedAction ?: return
		val handler = userProfileFeedActionHandler ?: return
		if (currentDestination !is MatchingNavKey) {
			return
		}

		handler(pendingAction.userId, pendingAction.action)
		pendingUserProfileFeedAction = null
	}
}

private class AuthGraphStateHolder(
	private val authSessionStore: AuthSessionStore,
	private val profileGateway: ProfileGateway,
	private val chatGateway: ChatGateway,
	private val pendingMatchActionStore: PendingMatchActionStore,
	private val fcmTokenRegistrar: FcmTokenRegistrar,
	private val backStack: SnapshotStateList<Any>
) {
	private val clearSessionState: suspend () -> Unit = {
		chatGateway.clearLocalData()
		pendingMatchActionStore.clear()
	}

	@Composable
	fun rememberState() = rememberAuthGraphState(
		backStack = backStack,
		authSessionStore = authSessionStore,
		onSessionCleared = clearSessionState,
		onSessionStarted = fcmTokenRegistrar::syncCurrentTokenIfAuthorized,
		resolveSignedInDestination = {
			AppStartDestinationResolver(
				authSessionStore = authSessionStore,
				profileGateway = profileGateway,
				clearSessionState = clearSessionState
			).resolveAuthenticatedDestination()
		}
	)
}

@Composable
fun rememberTMatchAppState(
	authSessionStore: AuthSessionStore,
	profileGateway: ProfileGateway,
	notificationGateway: NotificationGateway,
	chatGateway: ChatGateway,
	pendingMatchActionStore: PendingMatchActionStore
): TMatchAppState? {
	val clearSessionState: suspend () -> Unit = {
		chatGateway.clearLocalData()
		pendingMatchActionStore.clear()
	}
	val startDestination by produceState<Any?>(
		initialValue = null,
		authSessionStore,
		profileGateway,
		chatGateway,
		pendingMatchActionStore
	) {
		value = AppStartDestinationResolver(
			authSessionStore = authSessionStore,
			profileGateway = profileGateway,
			clearSessionState = clearSessionState
		).resolve()
	}
	val resolvedStartDestination = startDestination ?: return null
	val appState = remember(
		resolvedStartDestination,
		authSessionStore,
		profileGateway,
		notificationGateway,
		chatGateway,
		pendingMatchActionStore
	) {
		TMatchAppState(
			startDestination = resolvedStartDestination,
			authSessionStore = authSessionStore,
			profileGateway = profileGateway,
			notificationGateway = notificationGateway,
			chatGateway = chatGateway,
			pendingMatchActionStore = pendingMatchActionStore
		)
	}

	LaunchedEffect(appState.currentDestination) {
		appState.syncDatingProfileAccess()
	}

	LaunchedEffect(Unit) {
		appState.syncCurrentTokenIfAuthorized()
	}

	return appState
}

fun Any?.isMainTabDestination(): Boolean {
	return this is ProfileNavKey || this is MatchingNavKey || this is ChatsNavKey
}
