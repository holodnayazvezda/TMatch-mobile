package com.prod.tmatch_api.match.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RecommendationCandidateCardDto(
	@SerialName("user_id")
	val userId: String,
	val profile: RecommendationCandidateProfileDto? = null,
	@SerialName("dating_profile")
	val datingProfile: RecommendationCandidateDatingProfileDto? = null
)
