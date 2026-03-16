package com.prod.tmatch_api.profile.data.gateway

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
import org.junit.Assert.assertTrue
import org.junit.Test

class KtorProfileGatewayTest {

	@Test
	fun `maps get user card response to domain model`() = runBlocking {
		val gateway = KtorProfileGateway(
			httpClient = createHttpClient(
				status = HttpStatusCode.OK,
				body = """
					{
					  "user_id": "candidate-1",
					  "profile": {
					    "first_name": "Anna",
					    "last_name": "Smirnova",
					    "birth_date": "1997-04-03",
					    "gender": "female",
					    "region": "Saint Petersburg",
					    "avatar_url": "https://example.com/anna.jpg"
					  },
					  "dating_profile": {
					    "photos": [
					      "https://example.com/photo-1.jpg",
					      "https://example.com/photo-2.jpg"
					    ],
					    "traits": [
					      {
					        "trait_code": "kind",
					        "score": 0.93,
					        "is_hidden": false
					      },
					      {
					        "trait_code": "active_lifestyle",
					        "score": 0.81,
					        "is_hidden": true
					      }
					    ]
					  }
					}
				""".trimIndent()
			),
			authSessionStore = FakeAuthSessionStore()
		)

		val result = gateway.getUserCard("candidate-1")

		assertTrue(result is ApiOperationResult.Success)
		val card = (result as ApiOperationResult.Success).data
		assertEquals("candidate-1", card.userId)
		assertEquals("Anna", card.profile?.firstName)
		assertEquals("Smirnova", card.profile?.lastName)
		assertEquals("1997-04-03", card.profile?.birthDate)
		assertEquals("female", card.profile?.gender)
		assertEquals("Saint Petersburg", card.profile?.region)
		assertEquals("https://example.com/anna.jpg", card.profile?.avatarUrl)
		assertEquals(
			listOf("https://example.com/photo-1.jpg", "https://example.com/photo-2.jpg"),
			card.datingProfile?.photos
		)
		assertEquals(2, card.datingProfile?.traits?.size)
		assertEquals("kind", card.datingProfile?.traits?.get(0)?.traitCode)
		assertEquals(0.93f, card.datingProfile?.traits?.get(0)?.score)
		assertEquals(false, card.datingProfile?.traits?.get(0)?.isHidden)
	}

	@Test
	fun `returns api error when get user card payload cannot be parsed`() = runBlocking {
		val gateway = KtorProfileGateway(
			httpClient = createHttpClient(
				status = HttpStatusCode.OK,
				body = """{"user_id":"candidate-1","profile":{"first_name":42}}"""
			),
			authSessionStore = FakeAuthSessionStore()
		)

		val result = gateway.getUserCard("candidate-1")

		assertTrue(result is ApiOperationResult.ApiError)
		val error = result as ApiOperationResult.ApiError
		assertEquals(ApiErrorReason.RequestFailed, error.reason)
		assertTrue(error.message?.isNotBlank() == true)
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

		override fun clear() = Unit
	}
}
