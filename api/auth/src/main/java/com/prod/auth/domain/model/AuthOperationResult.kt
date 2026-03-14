package com.prod.auth.domain.model

enum class AuthApiErrorReason {
	ServiceUnavailable,
	RequestFailed
}

sealed interface AuthOperationResult {
	data class Success(val session: AuthSession) : AuthOperationResult
	data class ValidationError(val message: String? = null) : AuthOperationResult
	data class ApiError(
		val reason: AuthApiErrorReason,
		val message: String? = null
	) : AuthOperationResult
	data object NoInternet : AuthOperationResult
}
