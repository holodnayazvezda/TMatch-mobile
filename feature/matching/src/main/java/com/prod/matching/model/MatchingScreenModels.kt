package com.prod.matching.model

import com.prod.common.api.model.RecommendationReasonSummary

sealed interface MatchingDeckItem {
	data class Profile(
		val id: String,
		val name: String,
		val age: Int?,
		val reasons: List<RecommendationReasonSummary>,
		val imageUrl: String?,
		val recommendationId: String? = null,
		val isIncomingLike: Boolean = false
	) : MatchingDeckItem

	data class Transition(
		val id: String = "recommendations-transition"
	) : MatchingDeckItem
}

typealias MatchProfile = MatchingDeckItem.Profile
