package com.prod.matching.domain.model

data class PendingMatchAction(
	val profileId: String,
	val recommendationId: String?,
	val action: MatchAction
)
