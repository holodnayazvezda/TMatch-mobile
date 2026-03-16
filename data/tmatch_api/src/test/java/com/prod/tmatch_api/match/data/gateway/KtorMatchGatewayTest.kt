package com.prod.tmatch_api.match.data.gateway

import com.prod.auth.domain.gateway.AuthSessionStore
import com.prod.common.api.model.ApiErrorReason
import com.prod.common.api.model.ApiOperationResult
import com.prod.common.api.model.UserSession
import com.prod.match.domain.model.InteractionAction
import com.prod.match.domain.model.RecommendationReasonType
import com.prod.match.domain.model.TargetGender
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

class KtorMatchGatewayTest {

	@Test
	fun `maps incoming likes response`() = runBlocking {
		val gateway = KtorMatchGateway(
			httpClient = createHttpClient(
				status = HttpStatusCode.OK,
				body = """
					{
					  "items": [
					    {
					      "liker_user_id": "candidate-1",
					      "reasons": {
					        "RESTAURANTS": 0.91,
					        "new_backend_reason": 0.42
					      },
					      "candidate_card": {
					        "user_id": "candidate-1",
					        "profile": {
					          "first_name": "Alex",
					          "birth_date": "1998-05-10"
					        },
					        "dating_profile": {
					          "photos": ["https://example.com/photo-1.jpg"],
					          "traits": []
					        }
					      }
					    }
					  ]
					}
				""".trimIndent()
			),
			authSessionStore = FakeAuthSessionStore()
		)

		val result = gateway.getIncomingLikes()

		assertTrue(result is ApiOperationResult.Success)
		val likes = (result as ApiOperationResult.Success).data
		assertEquals(1, likes.size)
		assertEquals("candidate-1", likes.single().likerUserId)
		assertEquals("Alex", likes.single().candidateCard?.profile?.firstName)
		assertEquals(
			listOf(RecommendationReasonType.RESTAURANTS, RecommendationReasonType.Unknown),
			likes.single().reasons.map { it.reasonType }
		)
		assertEquals(
			"https://example.com/photo-1.jpg",
			likes.single().candidateCard?.datingProfile?.photos?.single()
		)
	}

	@Test
	fun `maps known and unknown recommendation reasons from backend keys`() = runBlocking {
		val gateway = KtorMatchGateway(
			httpClient = createHttpClient(
				status = HttpStatusCode.OK,
				body = """
					{
					  "items": [
					    {
					      "ml_recommendation_id": "rec-1",
					      "user_id": "user-1",
					      "candidate_user_id": "candidate-1",
					      "reasons": {
					        "new_backend_reason": 0.91,
					        "ACTIVITY_FREQUENCY": 0.42
					      }
					    }
					  ]
					}
				""".trimIndent()
			),
			authSessionStore = FakeAuthSessionStore()
		)

		val result = gateway.getRecommendations()

		assertTrue(result is ApiOperationResult.Success)
		val recommendations = (result as ApiOperationResult.Success).data
		assertEquals(1, recommendations.size)
		assertEquals(2, recommendations.single().reasons.size)
		assertEquals(
			listOf(RecommendationReasonType.Unknown, RecommendationReasonType.ACTIVITY_FREQUENCY),
			recommendations.single().reasons.map { it.reasonType }
		)
	}

	@Test
	fun `maps targeting response`() = runBlocking {
		val gateway = KtorMatchGateway(
			httpClient = createHttpClient(
				status = HttpStatusCode.OK,
				body = """
					{
					  "region": "Москва",
					  "gender_target": "female",
					  "age_from": 24,
					  "age_to": 31
					}
				""".trimIndent()
			),
			authSessionStore = FakeAuthSessionStore()
		)

		val result = gateway.getOwnTargeting()

		assertTrue(result is ApiOperationResult.Success)
		val targeting = (result as ApiOperationResult.Success).data
		assertEquals("Москва", targeting.region)
		assertEquals(TargetGender.Female, targeting.genderTarget)
		assertEquals(24, targeting.ageFrom)
		assertEquals(31, targeting.ageTo)
	}

	@Test
	fun `returns request failed for unknown target gender`() = runBlocking {
		val gateway = KtorMatchGateway(
			httpClient = createHttpClient(
				status = HttpStatusCode.OK,
				body = """
					{
					  "gender_target": "everyone",
					  "age_from": 18,
					  "age_to": 25
					}
				""".trimIndent()
			),
			authSessionStore = FakeAuthSessionStore()
		)

		val result = gateway.getOwnTargeting()

		assertEquals(
			ApiOperationResult.ApiError(
				reason = ApiErrorReason.RequestFailed,
				message = "Unsupported target gender in response"
			),
			result
		)
	}

	@Test
	fun `returns request failed instead of crashing on unknown interaction action`() = runBlocking {
		val gateway = KtorMatchGateway(
			httpClient = createHttpClient(
				status = HttpStatusCode.OK,
				body = """
					{
					  "interaction_id": "interaction-1",
					  "actor_user_id": "user-1",
					  "candidate_user_id": "candidate-1",
					  "action": "super_like",
					  "created_at": "2026-03-15T12:00:00Z"
					}
				""".trimIndent()
			),
			authSessionStore = FakeAuthSessionStore()
		)

		val result = gateway.createInteraction(
			candidateUserId = "candidate-1",
			action = com.prod.match.domain.model.InteractionAction.Like,
			mlRecommendationId = null
		)

		assertEquals(
			ApiOperationResult.ApiError(
				reason = ApiErrorReason.RequestFailed,
				message = "Unsupported interaction action in response"
			),
			result
		)
	}

	@Test
	fun `maps block interaction action from backend response`() = runBlocking {
		val gateway = KtorMatchGateway(
			httpClient = createHttpClient(
				status = HttpStatusCode.OK,
				body = """
					{
					  "interaction_id": "interaction-1",
					  "actor_user_id": "user-1",
					  "candidate_user_id": "candidate-1",
					  "action": "block",
					  "created_at": "2026-03-15T12:00:00Z"
					}
				""".trimIndent()
			),
			authSessionStore = FakeAuthSessionStore()
		)

		val result = gateway.createInteraction(
			candidateUserId = "candidate-1",
			action = InteractionAction.Block,
			mlRecommendationId = null
		)

		assertTrue(result is ApiOperationResult.Success)
		assertEquals(
			InteractionAction.Block,
			(result as ApiOperationResult.Success).data.action
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
