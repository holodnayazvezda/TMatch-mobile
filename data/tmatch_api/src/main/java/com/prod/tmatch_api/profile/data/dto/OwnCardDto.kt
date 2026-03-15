package com.prod.tmatch_api.profile.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OwnCardDto(
	@SerialName("user_id")
	val userId: String,
	val profile: CardProfileDto? = null,
	@SerialName("dating_profile")
	val datingProfile: CardDatingProfileDto? = null
)

@Serializable
data class CardProfileDto(
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

@Serializable
data class CardDatingProfileDto(
	val photos: List<String> = emptyList(),
	val traits: List<CardDatingTraitDto> = emptyList()
)

@Serializable
data class CardDatingTraitDto(
	@SerialName("trait_code")
	val traitCode: String,
	val score: Double,
	@SerialName("is_hidden")
	val isHidden: Boolean
)
