package com.prod.tmatch_api.auth.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AuthResponseDto(
	@SerialName("access_token")
	val accessToken: String,
	@SerialName("user_id")
	val userId: String
)
