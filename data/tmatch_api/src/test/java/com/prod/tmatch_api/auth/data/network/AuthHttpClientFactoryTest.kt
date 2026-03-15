package com.prod.tmatch_api.auth.data.network

import com.prod.tmatch_api.common.data.network.ApiResponseOverride
import com.prod.tmatch_api.common.data.network.InMemoryApiResponseOverrideRegistry
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.header
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.OutgoingContent
import io.ktor.http.headersOf
import io.ktor.utils.io.readRemaining
import io.ktor.utils.io.core.readBytes
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthHttpClientFactoryTest {

	@Test
	fun `returns override response when endpoint override is configured`() = runBlocking {
		val registry = InMemoryApiResponseOverrideRegistry().apply {
			setOverride(
				method = HttpMethod.Get,
				path = "/profile",
				response = ApiResponseOverride(
					statusCode = 503,
					body = """{"detail":"mocked outage"}"""
				)
			)
		}
		var fallbackCalled = false
		val fallbackClient = createFallbackClient {
			fallbackCalled = true
			respond(
				content = """{"userId":"real"}""",
				status = HttpStatusCode.OK,
				headers = jsonHeaders()
			)
		}

		val client = createAuthHttpClient(
			baseUrl = "https://example.com/",
			responseOverrideRegistry = registry,
			enableOverrides = true,
			fallbackClient = fallbackClient
		)

		try {
			val response = client.get("/profile")

			assertEquals(HttpStatusCode.ServiceUnavailable, response.status)
			assertEquals("""{"detail":"mocked outage"}""", response.bodyAsText())
			assertFalse(fallbackCalled)
		} finally {
			client.close()
			fallbackClient.close()
		}
	}

	@Test
	fun `forwards request to fallback client when no override exists`() = runBlocking {
		val registry = InMemoryApiResponseOverrideRegistry()
		var fallbackCalled = false
		val fallbackClient = createFallbackClient {
			fallbackCalled = true
			respond(
				content = """{"userId":"real"}""",
				status = HttpStatusCode.OK,
				headers = jsonHeaders()
			)
		}

		val client = createAuthHttpClient(
			baseUrl = "https://example.com/",
			responseOverrideRegistry = registry,
			enableOverrides = true,
			fallbackClient = fallbackClient
		)

		try {
			val response = client.get("/profile")

			assertEquals(HttpStatusCode.OK, response.status)
			assertEquals("""{"userId":"real"}""", response.bodyAsText())
			assertTrue(fallbackCalled)
		} finally {
			client.close()
			fallbackClient.close()
		}
	}

	@Test
	fun `returns override response for post endpoint when configured`() = runBlocking {
		val registry = InMemoryApiResponseOverrideRegistry().apply {
			setOverride(
				method = HttpMethod.Post,
				path = "/auth/registration/email",
				response = ApiResponseOverride(
					statusCode = 201,
					body = """{"access_token":"mock-token","user_id":"mock-user"}"""
				)
			)
		}
		var fallbackCalled = false
		val fallbackClient = createFallbackClient {
			fallbackCalled = true
			respond(
				content = """{"userId":"real"}""",
				status = HttpStatusCode.Created,
				headers = jsonHeaders()
			)
		}

		val client = createAuthHttpClient(
			baseUrl = "https://example.com/",
			responseOverrideRegistry = registry,
			enableOverrides = true,
			fallbackClient = fallbackClient
		)

		try {
			val response = client.post("/auth/registration/email")

			assertEquals(HttpStatusCode.Created, response.status)
			assertEquals("""{"access_token":"mock-token","user_id":"mock-user"}""", response.bodyAsText())
			assertFalse(fallbackCalled)
		} finally {
			client.close()
			fallbackClient.close()
		}
	}

	@Test
	fun `forwards post body to fallback client when no override exists`() = runBlocking {
		val registry = InMemoryApiResponseOverrideRegistry()
		var forwardedBody = ""
		val fallbackClient = createFallbackClient { request ->
			forwardedBody = request.body.readBodyAsText()
			respond(
				content = """{"status":"ok"}""",
				status = HttpStatusCode.OK,
				headers = jsonHeaders()
			)
		}

		val client = createAuthHttpClient(
			baseUrl = "https://example.com/",
			responseOverrideRegistry = registry,
			enableOverrides = true,
			fallbackClient = fallbackClient
		)

		try {
			client.post("/auth/registration/email") {
				header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
				setBody(SignUpPayload(email = "max@example.com", password = "secret"))
			}

			assertTrue(forwardedBody.contains("max@example.com"))
			assertTrue(forwardedBody.contains("secret"))
		} finally {
			client.close()
			fallbackClient.close()
		}
	}

	private fun createFallbackClient(
		handler: suspend io.ktor.client.engine.mock.MockRequestHandleScope.(HttpRequestData) -> HttpResponseData
	): HttpClient {
		return HttpClient(MockEngine(handler))
	}

	private fun jsonHeaders() = headersOf(
		name = io.ktor.http.HttpHeaders.ContentType,
		values = listOf(ContentType.Application.Json.toString())
	)

	private suspend fun Any.readBodyAsText(): String {
		return when (this) {
			is OutgoingContent.ByteArrayContent -> bytes().decodeToString()
			is OutgoingContent.ReadChannelContent -> readFrom().readRemaining().readBytes().decodeToString()
			is OutgoingContent.WriteChannelContent -> {
				val channel = io.ktor.utils.io.ByteChannel(autoFlush = true)
				writeTo(channel)
				channel.close(null)
				channel.readRemaining().readBytes().decodeToString()
			}
			is OutgoingContent.NoContent -> ""
			else -> toString()
		}
	}

	@Serializable
	private data class SignUpPayload(
		val email: String,
		val password: String
	)
}
