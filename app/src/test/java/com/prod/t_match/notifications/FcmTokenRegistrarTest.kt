package com.prod.t_match.notifications

import com.prod.auth.domain.gateway.AuthSessionStore
import com.prod.common.api.gateway.NotificationGateway
import com.prod.common.api.model.ApiOperationResult
import com.prod.common.api.model.UserSession
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class FcmTokenRegistrarTest {

	@Test
	fun `register token if authorized calls gateway`() = runBlocking {
		val gateway = FakeNotificationGateway()
		val registrar = FcmTokenRegistrar(
			authSessionStore = FakeAuthSessionStore(
				UserSession(accessToken = "token", userId = "user-1")
			),
			notificationGateway = gateway,
			logger = NoOpLogger
		)

		registrar.registerTokenIfAuthorized("fcm-token-1")

		assertEquals(listOf("fcm-token-1"), gateway.registeredTokens)
	}

	@Test
	fun `register token if authorized skips when session is missing`() = runBlocking {
		val gateway = FakeNotificationGateway()
		val registrar = FcmTokenRegistrar(
			authSessionStore = FakeAuthSessionStore(null),
			notificationGateway = gateway,
			logger = NoOpLogger
		)

		registrar.registerTokenIfAuthorized("fcm-token-1")

		assertEquals(emptyList<String>(), gateway.registeredTokens)
	}

	private class FakeNotificationGateway : NotificationGateway {
		val registeredTokens = mutableListOf<String>()

		override suspend fun registerDeviceToken(deviceToken: String): ApiOperationResult<Unit> {
			registeredTokens += deviceToken
			return ApiOperationResult.Success(Unit)
		}
	}

	private class FakeAuthSessionStore(
		private var session: UserSession?
	) : AuthSessionStore {
		override fun get(): UserSession? = session

		override fun save(session: UserSession) {
			this.session = session
		}

		override fun isProfileCompleted(): Boolean = false

		override fun setProfileCompleted(isCompleted: Boolean) = Unit

		override fun hasDatingProfile(): Boolean = false

		override fun setHasDatingProfile(hasDatingProfile: Boolean) = Unit

		override fun clear() {
			session = null
		}
	}

	private object NoOpLogger : FcmTokenRegistrar.Logger {
		override fun debug(message: String) = Unit

		override fun warn(message: String, error: Throwable?) = Unit
	}
}
