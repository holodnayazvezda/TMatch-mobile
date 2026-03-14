package com.prod.auth.data.gateway

import com.prod.auth.data.dto.ApiErrorDto
import com.prod.auth.data.dto.AuthResponseDto
import com.prod.auth.data.dto.EmailSignInRequestDto
import com.prod.auth.data.dto.EmailSignUpRequestDto
import com.prod.auth.data.network.createAuthHttpClient
import com.prod.auth.domain.model.AuthApiErrorReason
import com.prod.auth.domain.gateway.AuthGateway
import com.prod.auth.domain.model.AuthOperationResult
import com.prod.auth.domain.model.AuthSession
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
	private val baseUrl: String,
	private val httpClient: HttpClient = createAuthHttpClient()
) : AuthGateway {

	override suspend fun signInWithEmail(email: String, password: String): AuthOperationResult {
		return performAuthRequest(
			path = "/auth/signing/email",
			body = EmailSignInRequestDto(
				email = email,
				password = password
			)
		)
	}

	override suspend fun signUpWithEmail(email: String, password: String): AuthOperationResult {
		return performAuthRequest(
			path = "/auth/registration/email",
			body = EmailSignUpRequestDto(
				email = email,
				password = password
			)
		)
	}

	private suspend fun performAuthRequest(path: String, body: Any): AuthOperationResult {
		return try {
			val response = httpClient.post("${baseUrl.trimEnd('/')}$path") {
				accept(ContentType.Application.Json)
				header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
				setBody(body)
			}

			if (response.status.isSuccess()) {
				val payload = response.body<AuthResponseDto>()
				AuthOperationResult.Success(
					session = AuthSession(
						accessToken = payload.accessToken,
						userId = payload.userId
					)
				)
			} else {
				val errorPayload = runCatching { response.body<ApiErrorDto>() }.getOrNull()
				when (response.status) {
					HttpStatusCode.BadRequest -> AuthOperationResult.ValidationError(
						message = errorPayload?.detail
					)

					else -> AuthOperationResult.ApiError(
						reason = AuthApiErrorReason.ServiceUnavailable,
						message = errorPayload?.detail
					)
				}
			}
		} catch (_: UnresolvedAddressException) {
			AuthOperationResult.NoInternet
		} catch (_: UnknownHostException) {
			AuthOperationResult.NoInternet
		} catch (_: ConnectException) {
			AuthOperationResult.NoInternet
		} catch (_: NoRouteToHostException) {
			AuthOperationResult.NoInternet
		} catch (exception: Exception) {
			AuthOperationResult.ApiError(
				reason = AuthApiErrorReason.RequestFailed,
				message = exception.message?.takeIf { it.isNotBlank() }
			)
		}
	}
}
