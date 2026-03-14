package com.prod.tmatch_api.auth.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class EmailSignUpRequestDto(
	val email: String,
	val password: String
)
