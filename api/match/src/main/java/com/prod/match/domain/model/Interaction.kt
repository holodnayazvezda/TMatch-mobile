package com.prod.match.domain.model

data class Interaction(
	val interactionId: String,
	val actorUserId: String,
	val candidateUserId: String,
	val action: InteractionAction,
	val createdAt: String,
	val mlRecommendationId: String?
)
