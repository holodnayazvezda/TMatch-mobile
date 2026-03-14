package com.prod.tmatch_api.auth.data.gateway

import com.prod.auth.domain.gateway.AuthGateway
import com.prod.common.api.model.ApiErrorReason
import com.prod.common.api.model.ApiOperationResult
import com.prod.common.api.model.UserSession
import com.prod.tmatch_api.auth.data.dto.ApiErrorDto
import com.prod.tmatch_api.auth.data.dto.AuthResponseDto
import com.prod.tmatch_api.auth.data.dto.EmailSignInRequestDto
import com.prod.tmatch_api.auth.data.dto.EmailSignUpRequestDto
import com.prod.tmatch_api.auth.data.network.createAuthHttpClient
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.accept
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.isSuccess
import io.ktor.util.network.UnresolvedAddressException
import java.net.ConnectException
import java.net.NoRouteToHostException
import java.net.UnknownHostException

class KtorAuthGateway(
	private val httpClient: HttpClient
) : AuthGateway {

	override suspend fun signInWithEmail(
		email: String,
		password: String
	): ApiOperationResult<UserSession> {
		return performAuthRequest(
			path = "/auth/signing/email",
			body = EmailSignInRequestDto(
				email = email,
				password = password
			)
		)
	}

	override suspend fun signUpWithEmail(
		email: String,
		password: String
	): ApiOperationResult<UserSession> {
		return performAuthRequest(
			path = "/auth/registration/email",
			body = EmailSignUpRequestDto(
				email = email,
				password = password
			)
		)
	}

	private suspend fun performAuthRequest(
		path: String,
		body: Any
	): ApiOperationResult<UserSession> {
		return try {
			val response = httpClient.post(path) {
				accept(ContentType.Application.Json)
				header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
				setBody(body)
			}

			if (response.status.isSuccess()) {
				val payload = response.body<AuthResponseDto>()
				ApiOperationResult.Success(
					data = UserSession(
						accessToken = payload.accessToken,
						userId = payload.userId
					)
				)
			} else {
				val errorPayload = runCatching { response.body<ApiErrorDto>() }.getOrNull()
				when (response.status) {
					HttpStatusCode.BadRequest -> ApiOperationResult.ValidationError(
						message = errorPayload?.detail
					)

					HttpStatusCode.InternalServerError,
					HttpStatusCode.BadGateway,
					HttpStatusCode.ServiceUnavailable,
					HttpStatusCode.GatewayTimeout -> ApiOperationResult.ApiError(
						reason = ApiErrorReason.ServiceUnavailable,
						message = errorPayload?.detail
					)

					else -> ApiOperationResult.ApiError(
						reason = ApiErrorReason.RequestFailed,
						message = errorPayload?.detail
					)
				}
			}
		} catch (_: UnresolvedAddressException) {
			ApiOperationResult.NoInternet
		} catch (_: UnknownHostException) {
			ApiOperationResult.NoInternet
		} catch (_: ConnectException) {
			ApiOperationResult.NoInternet
		} catch (_: NoRouteToHostException) {
			ApiOperationResult.NoInternet
		} catch (exception: Exception) {
			ApiOperationResult.ApiError(
				reason = ApiErrorReason.RequestFailed,
				message = exception.message?.takeIf { it.isNotBlank() }
			)
		}
	}
}
