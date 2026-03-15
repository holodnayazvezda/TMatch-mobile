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
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.http.HeadersBuilder
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.takeFrom
import io.ktor.serialization.kotlinx.json.json
import io.ktor.utils.io.ByteReadChannel
import kotlinx.serialization.json.Json

private const val HTTP_LOG_TAG = "AuthHttpClient"

fun createAuthHttpClient(
	baseUrl: String,
	responseOverrideRegistry: ApiResponseOverrideRegistry? = null,
	enableOverrides: Boolean = false,
	fallbackClient: HttpClient? = null
): HttpClient {
	val networkClient = fallbackClient ?: createConfiguredHttpClient(
		engine = OkHttp.create(),
		baseUrl = baseUrl
	)

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

private suspend fun MockRequestHandleScope.forwardRequest(
	networkClient: HttpClient,
	request: HttpRequestData
) = with(request) {
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

		if (body !is io.ktor.http.content.OutgoingContent.NoContent) {
			setBody(body)
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
