package com.prod.tmatch_api.auth.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UpsertProfileRequestDto(
	@SerialName("first_name")
	val firstName: String,
	@SerialName("last_name")
	val lastName: String? = null,
	@SerialName("birth_date")
	val birthDate: String,
	val gender: String,
	val region: String? = null,
	@SerialName("avatar_url")
	val avatarUrl: String? = null
)
