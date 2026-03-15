package com.prod.t_match.navigation

import com.prod.auth.domain.gateway.AuthSessionStore
import com.prod.auth.navigation.OnboardingNavKey
import com.prod.auth.navigation.ProfileSetupNavKey
import com.prod.common.api.model.ApiErrorReason
import com.prod.common.api.model.ApiOperationResult
import com.prod.common.api.model.UserSession
import com.prod.profile.domain.gateway.ProfileGateway
import com.prod.profile.domain.model.DatingProfile
import com.prod.profile.domain.model.DatingProfileDraft
import com.prod.profile.domain.model.OwnCard
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
			profileGateway = FakeProfileGateway()
		)

		val destination = resolver.resolve()

		assertTrue(destination is OnboardingNavKey)
	}

	@Test
	fun `returns profile when account profile exists without dating profile`() = runBlocking {
		val sessionStore = FakeAuthSessionStore().apply {
			save(UserSession(accessToken = "token", userId = "user"))
			setProfileCompleted(false)
		}
		val resolver = AppStartDestinationResolver(
			authSessionStore = sessionStore,
			profileGateway = FakeProfileGateway(
				ownCardResult = ApiOperationResult.Success(
					OwnCard(
						userId = "user",
						hasAccountProfile = true,
						hasDatingProfile = false
					)
				)
			)
		)

		val destination = resolver.resolve()

		assertEquals(ProfileNavKey(source = "startup_resolver"), destination)
		assertTrue(sessionStore.isProfileCompleted())
	}

	@Test
	fun `returns profile when account and dating profiles both exist`() = runBlocking {
		val sessionStore = FakeAuthSessionStore().apply {
			save(UserSession(accessToken = "token", userId = "user"))
			setProfileCompleted(false)
		}
		val resolver = AppStartDestinationResolver(
			authSessionStore = sessionStore,
			profileGateway = FakeProfileGateway(
				ownCardResult = ApiOperationResult.Success(
					OwnCard(
						userId = "user",
						hasAccountProfile = true,
						hasDatingProfile = true
					)
				)
			)
		)

		val destination = resolver.resolve()

		assertEquals(ProfileNavKey(source = "startup_resolver"), destination)
		assertTrue(sessionStore.isProfileCompleted())
	}

	@Test
	fun `returns profile setup when account profile is missing`() = runBlocking {
		val sessionStore = FakeAuthSessionStore().apply {
			save(UserSession(accessToken = "token", userId = "user"))
			setProfileCompleted(true)
		}
		val resolver = AppStartDestinationResolver(
			authSessionStore = sessionStore,
			profileGateway = FakeProfileGateway(
				ownCardResult = ApiOperationResult.Success(
					OwnCard(
						userId = "user",
						hasAccountProfile = false,
						hasDatingProfile = false
					)
				)
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
			profileGateway = FakeProfileGateway(
				ownCardResult = ApiOperationResult.NoInternet
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
			profileGateway = FakeProfileGateway(
				ownCardResult = ApiOperationResult.ApiError(ApiErrorReason.Unauthorized)
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

	private class FakeProfileGateway(
		private val ownCardResult: ApiOperationResult<OwnCard> =
			ApiOperationResult.ValidationError()
	) : ProfileGateway {
		override suspend fun getOwnCard(): ApiOperationResult<OwnCard> = ownCardResult

		override suspend fun getOwnDatingProfile(): ApiOperationResult<DatingProfile?> =
			ApiOperationResult.Success(null)

		override suspend fun upsertOwnDatingProfile(
			draft: DatingProfileDraft
		): ApiOperationResult<DatingProfile> = ApiOperationResult.ValidationError()

		override suspend fun uploadDatingProfilePhoto(
			contentType: String,
			bytes: ByteArray
		): ApiOperationResult<String> = ApiOperationResult.ValidationError()
	}
}
