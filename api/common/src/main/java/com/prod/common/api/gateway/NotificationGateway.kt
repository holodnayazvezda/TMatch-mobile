package com.prod.common.api.gateway

import com.prod.common.api.model.ApiOperationResult

interface NotificationGateway {
	suspend fun registerDeviceToken(deviceToken: String): ApiOperationResult<Unit>
}
