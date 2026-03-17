package com.prod.tmatch_api.notifications.data.gateway

import com.prod.auth.domain.gateway.AuthSessionStore
import com.prod.common.api.model.ApiErrorReason
import com.prod.common.api.model.ApiOperationResult
import com.prod.common.api.model.UserSession
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

class KtorNotificationGatewayTest {

	@Test
	fun `register device token returns success for successful response`() = runBlocking {
		val gateway = KtorNotificationGateway(
			httpClient = createHttpClient(
				status = HttpStatusCode.OK,
				body = """{}"""
			),
			authSessionStore = FakeAuthSessionStore()
		)

		val result = gateway.registerDeviceToken("token-1")

		assertEquals(ApiOperationResult.Success(Unit), result)
	}

	@Test
	fun `register device token maps unauthorized api error`() = runBlocking {
		val gateway = KtorNotificationGateway(
			httpClient = createHttpClient(
				status = HttpStatusCode.Unauthorized,
				body = """{"message":"unauthorized"}"""
			),
			authSessionStore = FakeAuthSessionStore()
		)

		val result = gateway.registerDeviceToken("token-1")

		assertEquals(
			ApiOperationResult.ApiError(
				reason = ApiErrorReason.Unauthorized,
				message = "unauthorized"
			),
			result
		)
	}

	private fun createHttpClient(
		status: HttpStatusCode,
		body: String
	): HttpClient {
		return HttpClient(
			MockEngine {
				respond(
					content = body,
					status = status,
					headers = headersOf(
						HttpHeaders.ContentType,
						listOf(ContentType.Application.Json.toString())
					)
				)
			}
		) {
			install(ContentNegotiation) {
				json(
					Json {
						ignoreUnknownKeys = true
						explicitNulls = false
					}
				)
			}
		}
	}

	private class FakeAuthSessionStore : AuthSessionStore {
		override fun get(): UserSession? = UserSession(accessToken = "token", userId = "user-1")

		override fun save(session: UserSession) = Unit

		override fun isProfileCompleted(): Boolean = false

		override fun setProfileCompleted(isCompleted: Boolean) = Unit

		override fun hasDatingProfile(): Boolean = false

		override fun setHasDatingProfile(hasDatingProfile: Boolean) = Unit

		override fun clear() = Unit
	}
}
