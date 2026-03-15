package com.prod.tmatch_api.common.data.network

import io.ktor.http.HttpMethod
import java.util.concurrent.ConcurrentHashMap

data class ApiResponseOverride(
	val statusCode: Int,
	val body: String,
	val contentType: String = "application/json",
	val headers: Map<String, String> = emptyMap()
)

data class ApiResponseOverrideEntry(
	val method: HttpMethod,
	val path: String,
	val response: ApiResponseOverride
)

interface ApiResponseOverrideRegistry {
	fun setOverride(
		method: HttpMethod,
		path: String,
		response: ApiResponseOverride
	)

	fun getOverride(
		method: HttpMethod,
		path: String
	): ApiResponseOverride?

	fun removeOverride(
		method: HttpMethod,
		path: String
	)

	fun clear()

	fun getAll(): List<ApiResponseOverrideEntry>
}

class InMemoryApiResponseOverrideRegistry : ApiResponseOverrideRegistry {
	private val overrides = ConcurrentHashMap<ApiEndpointKey, ApiResponseOverride>()

	override fun setOverride(
		method: HttpMethod,
		path: String,
		response: ApiResponseOverride
	) {
		overrides[ApiEndpointKey(method = method, path = path.normalizePath())] = response
	}

	override fun getOverride(
		method: HttpMethod,
		path: String
	): ApiResponseOverride? {
		return overrides[ApiEndpointKey(method = method, path = path.normalizePath())]
	}

	override fun removeOverride(
		method: HttpMethod,
		path: String
	) {
		overrides.remove(ApiEndpointKey(method = method, path = path.normalizePath()))
	}

	override fun clear() {
		overrides.clear()
	}

	override fun getAll(): List<ApiResponseOverrideEntry> {
		return overrides.entries
			.sortedWith(compareBy({ it.key.path }, { it.key.method.value }))
			.map { (key, response) ->
				ApiResponseOverrideEntry(
					method = key.method,
					path = key.path,
					response = response
				)
			}
	}

	private data class ApiEndpointKey(
		val method: HttpMethod,
		val path: String
	)
}

private fun String.normalizePath(): String {
	if (isBlank()) {
		return "/"
	}

	return trim().let { value ->
		if (value.startsWith("/")) value else "/$value"
	}
}
