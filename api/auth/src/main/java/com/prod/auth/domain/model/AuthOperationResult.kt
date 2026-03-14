package com.prod.auth.domain.model

sealed interface AuthOperationResult {
	data class Success(val session: AuthSession) : AuthOperationResult
	data class ValidationError(val message: String) : AuthOperationResult
	data class ApiError(val message: String) : AuthOperationResult
	data object NoInternet : AuthOperationResult
}
