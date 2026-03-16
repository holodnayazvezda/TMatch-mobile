package com.prod.match.domain.model

data class IncomingLike(
	val likerUserId: String,
	val candidateCard: RecommendationCandidateCard?
)
