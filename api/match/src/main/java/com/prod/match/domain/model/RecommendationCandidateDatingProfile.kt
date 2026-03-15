package com.prod.match.domain.model

data class RecommendationCandidateDatingProfile(
	val photos: List<String>,
	val traits: List<RecommendationCandidateDatingTrait>
)
