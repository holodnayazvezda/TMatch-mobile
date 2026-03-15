package com.prod.tmatch_api.match.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class InteractionResponseDto(
	@SerialName("interaction_id")
	val interactionId: String,
	@SerialName("actor_user_id")
	val actorUserId: String,
	@SerialName("candidate_user_id")
	val candidateUserId: String,
	val action: String,
	@SerialName("created_at")
	val createdAt: String,
	@SerialName("ml_recommendation_id")
	val mlRecommendationId: String? = null
)
