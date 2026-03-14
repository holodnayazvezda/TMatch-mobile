package com.prod.auth.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApiErrorDto(
	val detail: String,
	@SerialName("status_code")
	val statusCode: Int
)
