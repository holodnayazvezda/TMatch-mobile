package com.prod.tmatch_api.auth.data.gateway

import com.prod.auth.domain.gateway.AuthSessionStore
import com.prod.auth.domain.gateway.AuthGateway
import com.prod.auth.domain.model.Profile
import com.prod.auth.domain.model.ProfileDraft
import com.prod.common.api.model.ApiErrorReason
import com.prod.common.api.model.ApiOperationResult
import com.prod.common.api.model.UserSession
import com.prod.tmatch_api.auth.data.dto.ApiErrorDto
import com.prod.tmatch_api.auth.data.dto.AuthResponseDto
import com.prod.tmatch_api.auth.data.dto.EmailSignInRequestDto
import com.prod.tmatch_api.auth.data.dto.EmailSignUpRequestDto
import com.prod.tmatch_api.auth.data.dto.ProfileDto
import com.prod.tmatch_api.auth.data.dto.UpsertProfileRequestDto
import com.prod.tmatch_api.common.data.network.KtorGatewaySupport
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.isSuccess

class KtorAuthGateway(
	private val httpClient: HttpClient,
	private val authSessionStore: AuthSessionStore
) : AuthGateway {

	private val gatewaySupport = KtorGatewaySupport(authSessionStore)

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

	override suspend fun getOwnProfile(): ApiOperationResult<Profile?> {
		return try {
			val response = httpClient.get("/profile") {
				accept(ContentType.Application.Json)
				with(gatewaySupport) {
					applyAuthorizedRequest()
				}
			}

			when {
				response.status.isSuccess() -> {
					ApiOperationResult.Success(response.body<ProfileDto>().toDomain())
				}

				response.status == HttpStatusCode.NotFound -> {
					ApiOperationResult.Success(null)
				}

				else -> gatewaySupport.mapErrorResponse(
					response.status,
					responseBody = runCatching { response.body<ApiErrorDto>() }.getOrNull()
				)
			}
		} catch (exception: Exception) {
			gatewaySupport.handleException(exception)
		}
	}

	override suspend fun upsertOwnProfile(draft: ProfileDraft): ApiOperationResult<Profile> {
		return try {
			val response = httpClient.put("/profile") {
				accept(ContentType.Application.Json)
				header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
				with(gatewaySupport) {
					applyAuthorizedRequest()
				}
				setBody(
					UpsertProfileRequestDto(
						firstName = draft.firstName,
						lastName = draft.lastName,
						birthDate = draft.birthDate,
						region = draft.region,
						avatarUrl = draft.avatarUrl
					)
				)
			}

			if (response.status.isSuccess()) {
				ApiOperationResult.Success(response.body<ProfileDto>().toDomain())
			} else {
				gatewaySupport.mapErrorResponse(
					status = response.status,
					responseBody = runCatching { response.body<ApiErrorDto>() }.getOrNull()
				)
			}
		} catch (exception: Exception) {
			gatewaySupport.handleException(exception)
		}
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
					gatewaySupport.mapErrorResponse(
						status = response.status,
						responseBody = runCatching { response.body<ApiErrorDto>() }.getOrNull()
					)
				}
			} catch (exception: Exception) {
				gatewaySupport.handleException(exception)
			}
		}

	private fun ProfileDto.toDomain(): Profile {
		return Profile(
			userId = userId,
			firstName = firstName,
			lastName = lastName,
			birthDate = birthDate,
			region = region,
			avatarUrl = avatarUrl
		)
	}
}
