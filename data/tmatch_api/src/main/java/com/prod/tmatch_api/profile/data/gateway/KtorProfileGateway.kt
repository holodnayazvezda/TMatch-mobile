package com.prod.tmatch_api.profile.data.gateway

import com.prod.auth.domain.gateway.AuthSessionStore
import com.prod.common.api.model.ApiErrorReason
import com.prod.common.api.model.ApiOperationResult
import com.prod.tmatch_api.auth.data.dto.ApiErrorDto
import com.prod.tmatch_api.auth.data.dto.UploadDatingPhotoResponseDto
import com.prod.tmatch_api.profile.data.dto.DatingProfileDto
import com.prod.tmatch_api.profile.data.dto.UpsertDatingProfileRequestDto
import com.prod.profile.domain.gateway.ProfileGateway
import com.prod.profile.domain.model.DatingProfile
import com.prod.profile.domain.model.DatingProfileDraft
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
import io.ktor.util.network.UnresolvedAddressException
import java.net.ConnectException
import java.net.NoRouteToHostException
import java.net.UnknownHostException

class KtorProfileGateway(
	private val httpClient: HttpClient,
	private val authSessionStore: AuthSessionStore
) : ProfileGateway {

	override suspend fun getOwnDatingProfile(): ApiOperationResult<DatingProfile?> {
		return try {
			val response = httpClient.get("/dating-profile/me") {
				accept(ContentType.Application.Json)
				applyAuthorizedRequest()
			}

			when {
				response.status.isSuccess() -> {
					ApiOperationResult.Success(response.body<DatingProfileDto>().toDomain())
				}

				response.status == HttpStatusCode.NotFound -> {
					ApiOperationResult.Success(null)
				}

				else -> mapErrorResponse(
					status = response.status,
					responseBody = runCatching { response.body<ApiErrorDto>() }.getOrNull()
				)
			}
		} catch (exception: Exception) {
			handleException(exception)
		}
	}

	override suspend fun upsertOwnDatingProfile(
		draft: DatingProfileDraft
	): ApiOperationResult<DatingProfile> {
		return try {
			val response = httpClient.put("/dating-profile/me") {
				accept(ContentType.Application.Json)
				header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
				applyAuthorizedRequest()
				setBody(UpsertDatingProfileRequestDto(photos = draft.photos))
			}

			if (response.status.isSuccess()) {
				ApiOperationResult.Success(response.body<DatingProfileDto>().toDomain())
			} else {
				mapErrorResponse(
					status = response.status,
					responseBody = runCatching { response.body<ApiErrorDto>() }.getOrNull()
				)
			}
		} catch (exception: Exception) {
			handleException(exception)
		}
	}

	override suspend fun uploadDatingProfilePhoto(
		contentType: String,
		bytes: ByteArray
	): ApiOperationResult<String> {
		return try {
			val response = httpClient.post("/dating-profile/me/photos") {
				accept(ContentType.Application.Json)
				header(HttpHeaders.ContentType, contentType)
				applyAuthorizedRequest()
				setBody(bytes)
			}

			if (response.status.isSuccess()) {
				ApiOperationResult.Success(response.body<UploadDatingPhotoResponseDto>().photoUrl)
			} else {
				mapErrorResponse(
					status = response.status,
					responseBody = runCatching { response.body<ApiErrorDto>() }.getOrNull()
				)
			}
		} catch (exception: Exception) {
			handleException(exception)
		}
	}

	private fun mapErrorResponse(
		status: HttpStatusCode,
		responseBody: ApiErrorDto?
	): ApiOperationResult<Nothing> {
		return when (status) {
			HttpStatusCode.Unauthorized -> {
				authSessionStore.clear()
				ApiOperationResult.ApiError(
					reason = ApiErrorReason.Unauthorized,
					message = responseBody?.detail
				)
			}

			HttpStatusCode.BadRequest -> ApiOperationResult.ValidationError(
				message = responseBody?.detail
			)

			HttpStatusCode.InternalServerError,
			HttpStatusCode.BadGateway,
			HttpStatusCode.ServiceUnavailable,
			HttpStatusCode.GatewayTimeout -> ApiOperationResult.ApiError(
				reason = ApiErrorReason.ServiceUnavailable,
				message = responseBody?.detail
			)

			else -> ApiOperationResult.ApiError(
				reason = ApiErrorReason.RequestFailed,
				message = responseBody?.detail
			)
		}
	}

	private fun handleException(exception: Exception): ApiOperationResult<Nothing> {
		return when (exception) {
			is UnresolvedAddressException,
			is UnknownHostException,
			is ConnectException,
			is NoRouteToHostException -> ApiOperationResult.NoInternet

			else -> ApiOperationResult.ApiError(
				reason = ApiErrorReason.RequestFailed,
				message = exception.message?.takeIf { it.isNotBlank() }
			)
		}
	}

	private fun io.ktor.client.request.HttpRequestBuilder.applyAuthorizedRequest() {
		val session = authSessionStore.get() ?: return
		header(HttpHeaders.Authorization, "Bearer ${session.accessToken}")
	}

	private fun DatingProfileDto.toDomain(): DatingProfile {
		return DatingProfile(
			userId = userId,
			photos = photos
		)
	}
}
