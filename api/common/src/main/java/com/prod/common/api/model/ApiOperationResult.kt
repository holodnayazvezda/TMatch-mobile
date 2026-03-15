package com.prod.common.api.model

enum class ApiErrorReason {
	Unauthorized,
	ServiceUnavailable,
	RequestFailed
}

sealed interface ApiOperationResult<out T> {
	data class Success<T>(val data: T) : ApiOperationResult<T>
	data class ValidationError(val message: String? = null) : ApiOperationResult<Nothing>
	data class ApiError(
		val reason: ApiErrorReason,
		val message: String? = null
	) : ApiOperationResult<Nothing>
	data object NoInternet : ApiOperationResult<Nothing>
}
