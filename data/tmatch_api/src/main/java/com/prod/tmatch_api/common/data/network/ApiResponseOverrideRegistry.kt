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
		val normalizedPath = path.normalizePath()
		return overrides[ApiEndpointKey(method = method, path = normalizedPath)]
			?: overrides.entries.firstOrNull { (key, _) ->
				key.method == method && key.path.matchesPathTemplate(normalizedPath)
			}?.value
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

private fun String.matchesPathTemplate(actualPath: String): Boolean {
	val templateSegments = trim('/').split('/').filter { it.isNotBlank() }
	val actualSegments = actualPath.trim('/').split('/').filter { it.isNotBlank() }

	if (templateSegments.size != actualSegments.size) {
		return false
	}

	return templateSegments.zip(actualSegments).all { (templateSegment, actualSegment) ->
		templateSegment == actualSegment || templateSegment.isPathPlaceholder()
	}
}

private fun String.isPathPlaceholder(): Boolean {
	return startsWith("{") && endsWith("}") && length > 2
}
