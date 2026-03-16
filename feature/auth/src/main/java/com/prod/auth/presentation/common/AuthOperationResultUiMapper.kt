package com.prod.auth.presentation.common

import com.prod.auth.R
import com.prod.common.api.model.ApiErrorReason
import com.prod.common.api.model.ApiOperationResult
import com.prod.common.api.model.sanitizeUserFacingApiMessage

class AuthOperationResultUiMapper {

	fun mapValidationError(error: ApiOperationResult.ValidationError): UiText {
		return error.message
			.sanitizeUserFacingApiMessage()
			?.let(UiText::DynamicString)
			?: UiText.StringResource(R.string.auth_error_invalid_input)
	}

	fun mapApiError(error: ApiOperationResult.ApiError): UiText {
		error.message
			.sanitizeUserFacingApiMessage()
			?.let(UiText::DynamicString)
			?.let { return it }

		return UiText.StringResource(
			resId = when (error.reason) {
				ApiErrorReason.Unauthorized -> R.string.auth_error_unauthorized
				ApiErrorReason.ServiceUnavailable -> R.string.auth_error_service_unavailable
				ApiErrorReason.RequestFailed -> R.string.auth_error_request_failed
			}
		)
	}
}
