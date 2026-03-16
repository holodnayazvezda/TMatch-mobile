package com.prod.tmatch_api.common.data.network

import io.ktor.http.HttpMethod
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ApiResponseOverrideRegistryTest {

	@Test
	fun `returns exact match before template match`() {
		val registry = InMemoryApiResponseOverrideRegistry()
		val exact = ApiResponseOverride(statusCode = 200, body = """{"source":"exact"}""")
		val template = ApiResponseOverride(statusCode = 200, body = """{"source":"template"}""")

		registry.setOverride(HttpMethod.Get, "/profile/card/{user_id}", template)
		registry.setOverride(HttpMethod.Get, "/profile/card/candidate-1", exact)

		val result = registry.getOverride(HttpMethod.Get, "/profile/card/candidate-1")

		assertEquals(exact, result)
	}

	@Test
	fun `matches template override for concrete path`() {
		val registry = InMemoryApiResponseOverrideRegistry()
		val template = ApiResponseOverride(statusCode = 200, body = """{"source":"template"}""")

		registry.setOverride(HttpMethod.Get, "/profile/card/{user_id}", template)

		val result = registry.getOverride(HttpMethod.Get, "/profile/card/candidate-42")

		assertEquals(template, result)
	}

	@Test
	fun `does not match template override when segment count differs`() {
		val registry = InMemoryApiResponseOverrideRegistry()

		registry.setOverride(
			HttpMethod.Get,
			"/profile/card/{user_id}",
			ApiResponseOverride(statusCode = 200, body = """{"source":"template"}""")
		)

		val result = registry.getOverride(HttpMethod.Get, "/profile/card/candidate-42/photos")

		assertNull(result)
	}
}
