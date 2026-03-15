package com.prod.tmatch_api.match.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RecommendationCandidateDatingTraitDto(
	@SerialName("trait_code")
	val traitCode: String,
	val score: Float,
	@SerialName("is_hidden")
	val isHidden: Boolean
)
