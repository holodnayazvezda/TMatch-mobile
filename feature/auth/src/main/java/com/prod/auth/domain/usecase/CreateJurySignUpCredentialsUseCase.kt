package com.prod.auth.domain.usecase

import com.prod.auth.domain.model.SignUpCredentials
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

class CreateJurySignUpCredentialsUseCase(
	private val clock: Clock = Clock.systemUTC()
) {
	private val timestampFormatter = DateTimeFormatter
		.ofPattern("yyyyMMddHHmmss")
		.withZone(ZoneOffset.UTC)

	operator fun invoke(): SignUpCredentials {
		// Временная метка делает тестовую почту уникальной для повторных проверок.
		val timestamp = timestampFormatter.format(Instant.now(clock))
		val password = "ProdContest2026!"

		return SignUpCredentials(
			email = "alina.morozova.$timestamp@example.com",
			password = password,
			confirmPassword = password
		)
	}
}
