package com.prod.profile.navigation

import com.prod.common.api.model.RecommendationReasonSummary

data class UserProfileNavKey(
	val userId: String,
	val source: String = "matching",
	val reasons: List<RecommendationReasonSummary> = emptyList(),
	val recommendationId: String? = null
)
