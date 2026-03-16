package com.prod.tmatch_api.common.data.network

import com.prod.auth.domain.gateway.AuthSessionStore
import com.prod.common.api.model.ApiOperationResult
import com.prod.common.api.model.UserSession
import org.junit.Assert.assertEquals
import org.junit.Test
import java.net.SocketTimeoutException

class KtorGatewaySupportTest {

	@Test
	fun `maps socket timeout to no internet`() {
		val support = KtorGatewaySupport(authSessionStore = FakeAuthSessionStore())

		val result = support.handleException(SocketTimeoutException("timeout"))

		assertEquals(ApiOperationResult.NoInternet, result)
	}

	private class FakeAuthSessionStore : AuthSessionStore {
		override fun get(): UserSession? = null

		override fun save(session: UserSession) = Unit

		override fun isProfileCompleted(): Boolean = false

		override fun setProfileCompleted(isCompleted: Boolean) = Unit

		override fun hasDatingProfile(): Boolean = false

		override fun setHasDatingProfile(hasDatingProfile: Boolean) = Unit

		override fun clear() = Unit
	}
}
