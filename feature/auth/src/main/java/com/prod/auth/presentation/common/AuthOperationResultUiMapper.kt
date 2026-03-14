package com.prod.auth.presentation.common

import com.prod.auth.R
import com.prod.auth.domain.model.AuthApiErrorReason
import com.prod.auth.domain.model.AuthOperationResult

class AuthOperationResultUiMapper {

	fun mapValidationError(error: AuthOperationResult.ValidationError): UiText {
		return error.message
			?.takeIf { it.isNotBlank() }
			?.let(UiText::DynamicString)
			?: UiText.StringResource(R.string.auth_error_invalid_input)
	}

	fun mapApiError(error: AuthOperationResult.ApiError): UiText {
		error.message
			?.takeIf { it.isNotBlank() }
			?.let(UiText::DynamicString)
			?.let { return it }

		return UiText.StringResource(
			resId = when (error.reason) {
				AuthApiErrorReason.ServiceUnavailable -> R.string.auth_error_service_unavailable
				AuthApiErrorReason.RequestFailed -> R.string.auth_error_request_failed
			}
		)
	}
}
