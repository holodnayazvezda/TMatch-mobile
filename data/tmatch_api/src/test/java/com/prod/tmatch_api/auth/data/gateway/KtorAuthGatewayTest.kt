package com.prod.tmatch_api.auth.data.gateway

import com.prod.auth.domain.gateway.AuthSessionStore
import com.prod.auth.domain.model.Gender
import com.prod.auth.domain.model.ProfileDraft
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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class KtorAuthGatewayTest {

	@Test
	fun `sign in maps session from backend response`() = runBlocking {
		val gateway = KtorAuthGateway(
			httpClient = createHttpClient(
				status = HttpStatusCode.OK,
				body = """
					{
					  "access_token": "access-token-1",
					  "user_id": "user-1"
					}
				""".trimIndent()
			),
			authSessionStore = FakeAuthSessionStore()
		)

		val result = gateway.signInWithEmail("user@example.com", "password")

		assertEquals(
			ApiOperationResult.Success(
				UserSession(
					accessToken = "access-token-1",
					userId = "user-1"
				)
			),
			result
		)
	}

	@Test
	fun `sign up maps session from backend response`() = runBlocking {
		val gateway = KtorAuthGateway(
			httpClient = createHttpClient(
				status = HttpStatusCode.OK,
				body = """
					{
					  "access_token": "access-token-2",
					  "user_id": "user-2"
					}
				""".trimIndent()
			),
			authSessionStore = FakeAuthSessionStore()
		)

		val result = gateway.signUpWithEmail("new@example.com", "password")

		assertEquals(
			ApiOperationResult.Success(
				UserSession(
					accessToken = "access-token-2",
					userId = "user-2"
				)
			),
			result
		)
	}

	@Test
	fun `get own profile returns null for not found`() = runBlocking {
		val gateway = KtorAuthGateway(
			httpClient = createHttpClient(
				status = HttpStatusCode.NotFound,
				body = """{"message":"not found"}"""
			),
			authSessionStore = FakeAuthSessionStore()
		)

		val result = gateway.getOwnProfile()

		assertTrue(result is ApiOperationResult.Success)
		assertNull((result as ApiOperationResult.Success).data)
	}

	@Test
	fun `upsert own profile maps created profile`() = runBlocking {
		val gateway = KtorAuthGateway(
			httpClient = createHttpClient(
				status = HttpStatusCode.OK,
				body = """
					{
					  "user_id": "user-1",
					  "first_name": "Anna",
					  "last_name": "Smirnova",
					  "birth_date": "1997-04-03",
					  "gender": "female",
					  "region": "Saint Petersburg",
					  "avatar_url": "https://example.com/avatar.jpg"
					}
				""".trimIndent()
			),
			authSessionStore = FakeAuthSessionStore()
		)

		val result = gateway.upsertOwnProfile(
			ProfileDraft(
				firstName = "Anna",
				lastName = "Smirnova",
				birthDate = "1997-04-03",
				gender = Gender.Female,
				region = "Saint Petersburg",
				avatarUrl = "https://example.com/avatar.jpg"
			)
		)

		assertTrue(result is ApiOperationResult.Success)
		val profile = (result as ApiOperationResult.Success).data
		assertEquals("user-1", profile.userId)
		assertEquals("Anna", profile.firstName)
		assertEquals("Smirnova", profile.lastName)
		assertEquals("1997-04-03", profile.birthDate)
		assertEquals(Gender.Female, profile.gender)
		assertEquals("Saint Petersburg", profile.region)
		assertEquals("https://example.com/avatar.jpg", profile.avatarUrl)
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
