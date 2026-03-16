package com.prod.tmatch_api.notifications.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RegisterDeviceBodyDto(
	@SerialName("device_id")
	val deviceId: String
)
