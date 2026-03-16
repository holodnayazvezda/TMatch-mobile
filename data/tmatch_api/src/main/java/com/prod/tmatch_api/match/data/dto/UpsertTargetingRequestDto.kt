package com.prod.tmatch_api.match.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UpsertTargetingRequestDto(
	@SerialName("region")
	val region: String? = null,
	@SerialName("gender_target")
	val genderTarget: String,
	@SerialName("age_from")
	val ageFrom: Int,
	@SerialName("age_to")
	val ageTo: Int
)
