package com.prod.common.api.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ApiErrorMessageSanitizerTest {

	@Test
	fun `maps technical connection errors to service unavailable message`() {
		assertEquals(
			SERVICE_UNAVAILABLE_USER_MESSAGE,
			"connection closed".sanitizeUserFacingApiMessage()
		)
		assertEquals(
			SERVICE_UNAVAILABLE_USER_MESSAGE,
			"socket timeout has expired".sanitizeUserFacingApiMessage()
		)
	}

	@Test
	fun `keeps user facing backend messages unchanged`() {
		assertEquals(
			"Пользователь не найден",
			"Пользователь не найден".sanitizeUserFacingApiMessage()
		)
	}

	@Test
	fun `returns null for blank input`() {
		assertNull(null.sanitizeUserFacingApiMessage())
		assertNull("   ".sanitizeUserFacingApiMessage())
	}
}
