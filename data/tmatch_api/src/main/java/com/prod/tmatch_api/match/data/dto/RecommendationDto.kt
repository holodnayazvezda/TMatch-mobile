package com.prod.tmatch_api.match.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RecommendationDto(
	@SerialName("ml_recommendation_id")
	val mlRecommendationId: String,
	@SerialName("user_id")
	val userId: String,
	@SerialName("candidate_user_id")
	val candidateUserId: String,
	val reasons: Map<String, Float>,
	@SerialName("candidate_card")
	val candidateCard: RecommendationCandidateCardDto? = null
)
