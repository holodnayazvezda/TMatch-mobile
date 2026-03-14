package com.prod.tmatch_api.auth.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApiErrorDto(
	val detail: String? = null
)
