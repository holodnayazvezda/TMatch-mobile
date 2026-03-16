package com.prod.match.domain.model

data class IncomingLike(
	val likerUserId: String,
	val reasons: List<RecommendationReason> = emptyList(),
	val candidateCard: RecommendationCandidateCard?
)
