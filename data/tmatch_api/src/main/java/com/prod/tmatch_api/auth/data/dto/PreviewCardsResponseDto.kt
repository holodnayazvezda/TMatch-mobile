package com.prod.tmatch_api.auth.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PreviewCardsResponseDto(
	val cards: List<PreviewCardDto> = emptyList()
)

@Serializable
data class PreviewCardDto(
	@SerialName("user_id")
	val userId: String,
	val profile: PreviewProfileDto? = null
)

@Serializable
data class PreviewProfileDto(
	@SerialName("first_name")
	val firstName: String,
	@SerialName("last_name")
	val lastName: String? = null
)
