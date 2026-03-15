package com.prod.tmatch_api.common.data.network

import com.prod.auth.domain.gateway.AuthSessionStore
import com.prod.common.api.model.ApiErrorReason
import com.prod.common.api.model.ApiOperationResult
import com.prod.tmatch_api.auth.data.dto.ApiErrorDto
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.util.network.UnresolvedAddressException
import java.net.ConnectException
import java.net.NoRouteToHostException
import java.net.UnknownHostException

internal class KtorGatewaySupport(
	private val authSessionStore: AuthSessionStore
) {
	private fun ApiErrorDto?.resolvedMessage(): String? {
		return this?.detail ?: this?.message
	}

	fun mapErrorResponse(
		status: HttpStatusCode,
		responseBody: ApiErrorDto?
	): ApiOperationResult<Nothing> {
		return when (status) {
			HttpStatusCode.Unauthorized -> {
				authSessionStore.clear()
				ApiOperationResult.ApiError(
					reason = ApiErrorReason.Unauthorized,
					message = responseBody.resolvedMessage()
				)
			}

			HttpStatusCode.BadRequest -> ApiOperationResult.ValidationError(
				message = responseBody.resolvedMessage()
			)

			HttpStatusCode.Conflict,
			HttpStatusCode.UnprocessableEntity -> ApiOperationResult.ValidationError(
				message = responseBody.resolvedMessage()
			)

			HttpStatusCode.InternalServerError,
			HttpStatusCode.BadGateway,
			HttpStatusCode.ServiceUnavailable,
			HttpStatusCode.GatewayTimeout -> ApiOperationResult.ApiError(
				reason = ApiErrorReason.ServiceUnavailable,
				message = responseBody.resolvedMessage()
			)

			else -> ApiOperationResult.ApiError(
				reason = ApiErrorReason.RequestFailed,
				message = responseBody.resolvedMessage()
			)
		}
	}

	fun handleException(exception: Exception): ApiOperationResult<Nothing> {
		return when (exception) {
			is UnresolvedAddressException,
			is UnknownHostException,
			is ConnectException,
			is NoRouteToHostException -> ApiOperationResult.NoInternet

			else -> {
				if (
					exception.hasCauseNamed("CertPathValidationException") ||
					exception.hasCauseNamed("CertPathValidatorException")
				) {
					ApiOperationResult.NoInternet
				} else {
					ApiOperationResult.ApiError(
						reason = ApiErrorReason.RequestFailed,
						message = exception.message?.takeIf { it.isNotBlank() }
					)
				}
			}
		}
	}

	fun HttpRequestBuilder.applyAuthorizedRequest() {
		val session = authSessionStore.get() ?: return
		header(HttpHeaders.Authorization, "Bearer ${session.accessToken}")
	}

	private fun Throwable.hasCauseNamed(simpleName: String): Boolean {
		var current: Throwable? = this
		while (current != null) {
			if (current::class.java.simpleName == simpleName) {
				return true
			}
			current = current.cause
		}
		return false
	}
}
