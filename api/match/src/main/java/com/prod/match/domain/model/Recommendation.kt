package com.prod.match.domain.model

data class Recommendation(
	val mlRecommendationId: String,
	val userId: String,
	val candidateUserId: String,
	val reasons: List<RecommendationReason>,
	val candidateCard: RecommendationCandidateCard?
)
