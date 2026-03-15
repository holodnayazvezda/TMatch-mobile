package com.prod.auth.navigation

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.prod.auth.domain.gateway.AuthSessionStore
import com.prod.common.api.model.UserSession
import com.prod.profile.navigation.ProfileNavKey
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthGraphStateTest {

	@Test
	fun `sign in routes existing user without dating profile to profile screen`() = runBlocking {
		val sessionStore = FakeAuthSessionStore()
		val backStack = mutableStateListOf<Any>(SignInNavKey())
		val state = createState(
			backStack = backStack,
			authSessionStore = sessionStore,
			resolveSignedInDestination = {
				ProfileNavKey(source = "signin_existing_without_dating")
			}
		)

		state.onSignInSuccess(UserSession(accessToken = "token", userId = "user"))

		assertEquals(
			listOf(ProfileNavKey(source = "signin_existing_without_dating")),
			backStack.toList()
		)
		assertEquals("token", sessionStore.get()?.accessToken)
	}

	@Test
	fun `sign in routes existing user with dating profile to profile screen`() = runBlocking {
		val sessionStore = FakeAuthSessionStore()
		val backStack = mutableStateListOf<Any>(SignInNavKey())
		val state = createState(
			backStack = backStack,
			authSessionStore = sessionStore,
			resolveSignedInDestination = {
				ProfileNavKey(source = "signin_existing_with_dating")
			}
		)

		state.onSignInSuccess(UserSession(accessToken = "token", userId = "user"))

		assertEquals(
			listOf(ProfileNavKey(source = "signin_existing_with_dating")),
			backStack.toList()
		)
	}

	@Test
	fun `sign in routes user without account profile to profile setup`() = runBlocking {
		val sessionStore = FakeAuthSessionStore()
		val backStack = mutableStateListOf<Any>(SignInNavKey())
		val state = createState(
			backStack = backStack,
			authSessionStore = sessionStore,
			resolveSignedInDestination = { ProfileSetupNavKey() }
		)

		state.onSignInSuccess(UserSession(accessToken = "token", userId = "user"))

		assertEquals(listOf(ProfileSetupNavKey()), backStack.toList())
	}

	@Test
	fun `sign up always routes to profile setup for new user`() = runBlocking {
		val sessionStore = FakeAuthSessionStore()
		val backStack = mutableStateListOf<Any>(SignUpNavKey())
		val state = createState(
			backStack = backStack,
			authSessionStore = sessionStore,
			resolveSignedInDestination = { ProfileNavKey() }
		)

		state.onSignUpSuccess(UserSession(accessToken = "token", userId = "user"))

		assertEquals(listOf(ProfileSetupNavKey()), backStack.toList())
		assertTrue(sessionStore.get() != null)
		assertEquals(false, sessionStore.isProfileCompleted())
	}

	@Test
	fun `logout clears session and opens onboarding then sign in`() {
		val sessionStore = FakeAuthSessionStore().apply {
			save(UserSession(accessToken = "token", userId = "user"))
			setProfileCompleted(true)
		}
		val pendingMatchActionStore = FakePendingMatchActionStore()
		val backStack = mutableStateListOf<Any>(ProfileNavKey())
		val state = createState(
			backStack = backStack,
			authSessionStore = sessionStore,
			pendingMatchActionStore = pendingMatchActionStore,
			resolveSignedInDestination = { ProfileNavKey() }
		)

		state.onLogout()

			assertEquals(listOf(OnboardingNavKey(), SignInNavKey()), backStack.toList())
			assertNull(sessionStore.get())
			assertEquals(false, sessionStore.isProfileCompleted())
			assertTrue(pendingMatchActionStore.wasCleared)
		}

	@Test
	fun `unauthorized clears session queue and opens onboarding`() {
		val sessionStore = FakeAuthSessionStore().apply {
			save(UserSession(accessToken = "token", userId = "user"))
			setProfileCompleted(true)
		}
		val pendingMatchActionStore = FakePendingMatchActionStore()
		val backStack = mutableStateListOf<Any>(ProfileNavKey())
		val state = createState(
			backStack = backStack,
			authSessionStore = sessionStore,
			pendingMatchActionStore = pendingMatchActionStore,
			resolveSignedInDestination = { ProfileNavKey() }
		)

		state.onUnauthorized()

		assertEquals(listOf(OnboardingNavKey()), backStack.toList())
		assertNull(sessionStore.get())
		assertTrue(pendingMatchActionStore.wasCleared)
	}

	private fun createState(
		backStack: SnapshotStateList<Any>,
		authSessionStore: AuthSessionStore,
		pendingMatchActionStore: FakePendingMatchActionStore = FakePendingMatchActionStore(),
		resolveSignedInDestination: suspend () -> Any
	): AuthGraphState {
		return AuthGraphState(
			backStack = backStack,
			authSessionStore = authSessionStore,
			onSessionCleared = pendingMatchActionStore::clear,
			resolveSignedInDestination = resolveSignedInDestination,
			consumeRetryActionInternal = { null },
			requestNoInternetInternal = {}
		)
	}

	private class FakeAuthSessionStore : AuthSessionStore {
		private var session: UserSession? = null
		private var isProfileCompleted = false

		override fun get(): UserSession? = session

		override fun save(session: UserSession) {
			this.session = session
		}

		override fun isProfileCompleted(): Boolean = isProfileCompleted

		override fun setProfileCompleted(isCompleted: Boolean) {
			isProfileCompleted = isCompleted
		}

		override fun clear() {
			session = null
			isProfileCompleted = false
		}
	}

	private class FakePendingMatchActionStore {
		var wasCleared: Boolean = false

		fun clear() {
			wasCleared = true
		}
	}
}
