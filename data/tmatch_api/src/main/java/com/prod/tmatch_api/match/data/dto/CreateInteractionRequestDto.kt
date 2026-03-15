package com.prod.tmatch_api.match.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateInteractionRequestDto(
	@SerialName("candidate_user_id")
	val candidateUserId: String,
	val action: String,
	@SerialName("ml_recommendation_id")
	val mlRecommendationId: String?
)
