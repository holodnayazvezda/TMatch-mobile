package com.prod.match.domain.model

data class RecommendationReason(
	val score: Float,
	val reasonType: RecommendationReasonType
)
