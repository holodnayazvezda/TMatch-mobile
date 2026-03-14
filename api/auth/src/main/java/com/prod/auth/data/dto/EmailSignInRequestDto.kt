package com.prod.auth.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class EmailSignInRequestDto(
	val email: String,
	val password: String
)
