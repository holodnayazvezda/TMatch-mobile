package com.prod.tmatch_api.match.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class IncomingLikeItemDto(
	@SerialName("liker_user_id")
	val likerUserId: String,
	val reasons: Map<String, Float>? = emptyMap(),
	@SerialName("candidate_card")
	val candidateCard: RecommendationCandidateCardDto? = null
)
