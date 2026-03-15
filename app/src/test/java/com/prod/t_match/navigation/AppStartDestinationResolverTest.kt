package com.prod.t_match.navigation

import com.prod.auth.domain.gateway.AuthGateway
import com.prod.auth.domain.gateway.AuthSessionStore
import com.prod.auth.domain.model.Gender
import com.prod.auth.domain.model.Profile
import com.prod.auth.domain.model.ProfileDraft
import com.prod.auth.navigation.OnboardingNavKey
import com.prod.auth.navigation.ProfileSetupNavKey
import com.prod.common.api.model.ApiErrorReason
import com.prod.common.api.model.ApiOperationResult
import com.prod.common.api.model.UserSession
import com.prod.profile.navigation.ProfileNavKey
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AppStartDestinationResolverTest {

	@Test
	fun `returns onboarding when there is no saved session`() = runBlocking {
		val sessionStore = FakeAuthSessionStore()
		val resolver = AppStartDestinationResolver(
			authSessionStore = sessionStore,
			authGateway = FakeAuthGateway()
		)

		val destination = resolver.resolve()

		assertTrue(destination is OnboardingNavKey)
	}

	@Test
	fun `returns profile when backend confirms existing profile`() = runBlocking {
		val sessionStore = FakeAuthSessionStore().apply {
			save(UserSession(accessToken = "token", userId = "user"))
			setProfileCompleted(false)
		}
		val resolver = AppStartDestinationResolver(
			authSessionStore = sessionStore,
			authGateway = FakeAuthGateway(
				ownProfileResult = ApiOperationResult.Success(
					Profile(
						userId = "user",
						firstName = "Max",
						lastName = null,
						birthDate = "2000-01-01",
						gender = Gender.Male,
						region = null,
						avatarUrl = null
					)
				)
			)
		)

		val destination = resolver.resolve()

		assertEquals(ProfileNavKey(source = "startup_resolver"), destination)
		assertTrue(sessionStore.isProfileCompleted())
	}

	@Test
	fun `returns profile setup when backend reports missing profile`() = runBlocking {
		val sessionStore = FakeAuthSessionStore().apply {
			save(UserSession(accessToken = "token", userId = "user"))
			setProfileCompleted(true)
		}
		val resolver = AppStartDestinationResolver(
			authSessionStore = sessionStore,
			authGateway = FakeAuthGateway(
				ownProfileResult = ApiOperationResult.Success(null)
			)
		)

		val destination = resolver.resolve()

		assertTrue(destination is ProfileSetupNavKey)
		assertEquals(false, sessionStore.isProfileCompleted())
	}

	@Test
	fun `falls back to cached profile state when network is unavailable`() = runBlocking {
		val sessionStore = FakeAuthSessionStore().apply {
			save(UserSession(accessToken = "token", userId = "user"))
			setProfileCompleted(true)
		}
		val resolver = AppStartDestinationResolver(
			authSessionStore = sessionStore,
			authGateway = FakeAuthGateway(
				ownProfileResult = ApiOperationResult.NoInternet
			)
		)

		val destination = resolver.resolve()

		assertEquals(ProfileNavKey(source = "startup_fallback"), destination)
	}

	@Test
	fun `clears session and returns onboarding on unauthorized response`() = runBlocking {
		val sessionStore = FakeAuthSessionStore().apply {
			save(UserSession(accessToken = "token", userId = "user"))
			setProfileCompleted(true)
		}
		val resolver = AppStartDestinationResolver(
			authSessionStore = sessionStore,
			authGateway = FakeAuthGateway(
				ownProfileResult = ApiOperationResult.ApiError(ApiErrorReason.Unauthorized)
			)
		)

		val destination = resolver.resolve()

		assertTrue(destination is OnboardingNavKey)
		assertNull(sessionStore.get())
	}

	private class FakeAuthSessionStore : AuthSessionStore {
		private var session: UserSession? = null
		private var isProfileCompleted: Boolean = false

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

	private class FakeAuthGateway(
		private val ownProfileResult: ApiOperationResult<Profile?> =
			ApiOperationResult.ValidationError()
	) : AuthGateway {
		override suspend fun signInWithEmail(
			email: String,
			password: String
		) = ApiOperationResult.ValidationError()

		override suspend fun signUpWithEmail(
			email: String,
			password: String
		) = ApiOperationResult.ValidationError()

		override suspend fun getOwnProfile(): ApiOperationResult<Profile?> = ownProfileResult

		override suspend fun upsertOwnProfile(
			draft: ProfileDraft
		): ApiOperationResult<Profile> = ApiOperationResult.ValidationError()
	}
}
