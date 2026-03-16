package com.prod.tmatch_api.notifications.data.gateway

import com.prod.auth.domain.gateway.AuthSessionStore
import com.prod.common.api.gateway.NotificationGateway
import com.prod.common.api.model.ApiOperationResult
import com.prod.tmatch_api.auth.data.dto.ApiErrorDto
import com.prod.tmatch_api.common.data.network.KtorGatewaySupport
import com.prod.tmatch_api.notifications.data.dto.RegisterDeviceBodyDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.accept
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess

class KtorNotificationGateway(
	private val httpClient: HttpClient,
	private val authSessionStore: AuthSessionStore
) : NotificationGateway {

	private val gatewaySupport = KtorGatewaySupport(authSessionStore)

	override suspend fun registerDeviceToken(deviceToken: String): ApiOperationResult<Unit> {
		return try {
			val response = httpClient.post("/notifications/devices") {
				accept(ContentType.Application.Json)
				header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
				with(gatewaySupport) {
					applyAuthorizedRequest()
				}
				setBody(RegisterDeviceBodyDto(deviceId = deviceToken))
			}

			if (response.status.isSuccess()) {
				ApiOperationResult.Success(Unit)
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
}
