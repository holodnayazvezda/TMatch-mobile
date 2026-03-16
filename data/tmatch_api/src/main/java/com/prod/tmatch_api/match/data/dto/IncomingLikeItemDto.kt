package com.prod.tmatch_api.match.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class IncomingLikeItemDto(
	@SerialName("liker_user_id")
	val likerUserId: String,
	@SerialName("candidate_card")
	val candidateCard: RecommendationCandidateCardDto? = null
)
