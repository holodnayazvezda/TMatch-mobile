package com.prod.tmatch_api.auth.data.network

import android.util.Log
import com.prod.tmatch_api.common.data.network.ApiResponseOverrideRegistry
import com.prod.tmatch_api.data.BuildConfig
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.http.HeadersBuilder
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.OutgoingContent
import io.ktor.http.takeFrom
import io.ktor.serialization.kotlinx.json.json
import io.ktor.utils.io.ByteChannel
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.core.readBytes
import io.ktor.utils.io.readRemaining
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okhttp3.MediaType.Companion.toMediaType

private const val HTTP_LOG_TAG = "AuthHttpClient"

fun createAuthHttpClient(
	baseUrl: String,
	responseOverrideRegistry: ApiResponseOverrideRegistry? = null,
	enableOverrides: Boolean = false,
	fallbackClient: HttpClient? = null
): HttpClient {
	if (fallbackClient == null) {
		val engine = OkHttp.create {
			if (enableOverrides && responseOverrideRegistry != null) {
				addInterceptor(ApiResponseOverrideInterceptor(responseOverrideRegistry))
			}
		}
		return createConfiguredHttpClient(
			engine = engine,
			baseUrl = baseUrl
		)
	}

	val networkClient = fallbackClient
	if (!enableOverrides || responseOverrideRegistry == null) {
		return networkClient
	}

	return createConfiguredHttpClient(
		engine = MockEngine { request ->
			val override = responseOverrideRegistry.getOverride(
				method = request.method,
				path = request.url.encodedPath
			)

			if (override != null) {
				debugLog("[MOCK] ${request.method.value} ${request.url.encodedPath} -> ${override.statusCode}")
				val headers = HeadersBuilder().apply {
					append(HttpHeaders.ContentType, override.contentType)
					override.headers.forEach { (name, value) -> append(name, value) }
				}.build()

				respond(
					content = ByteReadChannel(override.body),
					status = HttpStatusCode.fromValue(override.statusCode),
					headers = headers
				)
			} else {
				debugLog("[NETWORK] ${request.method.value} ${request.url.encodedPath}")
				forwardRequest(networkClient = networkClient, request = request)
			}
		},
		baseUrl = baseUrl
	)
}

private fun debugLog(message: String) {
	runCatching {
		Log.d(HTTP_LOG_TAG, message)
	}
}

private class ApiResponseOverrideInterceptor(
	private val responseOverrideRegistry: ApiResponseOverrideRegistry
) : Interceptor {
	override fun intercept(chain: Interceptor.Chain): Response {
		val request = chain.request()
		val override = responseOverrideRegistry.getOverride(
			method = io.ktor.http.HttpMethod.parse(request.method),
			path = request.url.encodedPath
		)

		if (override == null) {
			return chain.proceed(request)
		}

		debugLog("[MOCK] ${request.method} ${request.url.encodedPath} -> ${override.statusCode}")
		val responseHeaders = okhttp3.Headers.Builder().apply {
			add(HttpHeaders.ContentType, override.contentType)
			override.headers.forEach { (name, value) ->
				add(name, value)
			}
		}.build()

		return Response.Builder()
			.request(request)
			.protocol(Protocol.HTTP_1_1)
			.code(override.statusCode)
			.message(HttpStatusCode.fromValue(override.statusCode).description)
			.headers(responseHeaders)
			.body(override.body.toByteArray().toResponseBody(override.contentType.toMediaType()))
			.build()
	}
}

private suspend fun MockRequestHandleScope.forwardRequest(
	networkClient: HttpClient,
	request: HttpRequestData
) = with(request) {
	val requestBody = body
	val response = networkClient.request {
		method = request.method
		url.takeFrom(request.url)
		request.headers.entries().forEach { entry ->
			if (entry.key.equals(HttpHeaders.ContentLength, ignoreCase = true)) {
				return@forEach
			}
			entry.value.forEach { value ->
				headers.append(entry.key, value)
			}
		}

		when (requestBody) {
			is OutgoingContent.NoContent -> Unit
			is OutgoingContent -> {
				val payload = requestBody.toPayload()
				if (payload != null) {
					setBody(payload)
				}
				requestBody.headers.entries().forEach { entry ->
					if (
						entry.key.equals(HttpHeaders.ContentLength, ignoreCase = true) ||
						headers.contains(entry.key)
					) {
						return@forEach
					}
					entry.value.forEach { value ->
						headers.append(entry.key, value)
					}
				}
			}
			else -> setBody(requestBody)
		}
	}

	val responseBody: ByteArray = runCatching { response.body<ByteArray>() }.getOrElse { ByteArray(0) }
	val responseHeaders = HeadersBuilder().apply {
		response.headers.entries().forEach { entry ->
			entry.value.forEach { value ->
				append(entry.key, value)
			}
		}
	}.build()

	respond(
		content = ByteReadChannel(responseBody),
		status = response.status,
		headers = responseHeaders
	)
}

private suspend fun OutgoingContent.toPayload(): Any? {
	return when (this) {
		is OutgoingContent.NoContent -> null
		is OutgoingContent.ByteArrayContent -> bytes()
		is OutgoingContent.ReadChannelContent -> readFrom().readRemaining().readBytes()
		is OutgoingContent.WriteChannelContent -> {
			val channel = ByteChannel(autoFlush = true)
			writeTo(channel)
			channel.close(null)
			channel.readRemaining().readBytes()
		}
		is OutgoingContent.ProtocolUpgrade -> null
	}
}

private fun createConfiguredHttpClient(
	engine: HttpClientEngine,
	baseUrl: String
): HttpClient {
	return HttpClient(engine) {
		defaultRequest {
			url(baseUrl)
		}

		install(ContentNegotiation) {
			json(
				Json {
					ignoreUnknownKeys = true
					explicitNulls = false
				}
			)
		}

		install(WebSockets)

		if (BuildConfig.DEBUG) {
			install(Logging) {
				logger = object : Logger {
					override fun log(message: String) {
						Log.d(HTTP_LOG_TAG, message)
					}
				}
				level = LogLevel.BODY
				sanitizeHeader { header -> header == HttpHeaders.Authorization }
			}
		}
	}
}
