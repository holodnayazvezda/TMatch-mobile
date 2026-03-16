package com.prod.common.api.model

const val SERVICE_UNAVAILABLE_USER_MESSAGE = "Сервис временно недоступен"

private val technicalErrorMarkers = listOf(
	"connection closed",
	"connection reset",
	"connection refused",
	"connection abort",
	"failed to connect",
	"socket timeout",
	"timeout",
	"timed out",
	"deadline exceeded",
	"broken pipe",
	"unexpected end of stream",
	"stream was reset",
	"reset by peer",
	"eof",
	"ssl",
	"handshake",
	"certificate",
	"closed channel",
	"network is unreachable",
	"service unavailable",
	"gateway timeout",
	"bad gateway"
)

fun String?.sanitizeUserFacingApiMessage(): String? {
	val normalized = this
		?.trim()
		?.takeIf { it.isNotBlank() }
		?: return null

	val lowercaseMessage = normalized.lowercase()
	return if (technicalErrorMarkers.any(lowercaseMessage::contains)) {
		SERVICE_UNAVAILABLE_USER_MESSAGE
	} else {
		normalized
	}
}
