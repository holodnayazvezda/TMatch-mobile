package com.prod.matching.model

import com.prod.common.api.model.RecommendationReasonSummary

data class MatchProfile(
	val id: String,
	val name: String,
	val age: Int?,
	val reasons: List<RecommendationReasonSummary>,
	val imageUrl: String?,
	val recommendationId: String? = null
)
