package com.prod.match.domain.model

data class RecommendationCandidateCard(
	val userId: String,
	val profile: RecommendationCandidateProfile?,
	val datingProfile: RecommendationCandidateDatingProfile?
)
