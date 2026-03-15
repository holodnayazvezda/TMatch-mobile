package com.prod.match.domain.gateway

import com.prod.common.api.model.ApiOperationResult
import com.prod.match.domain.model.Interaction
import com.prod.match.domain.model.InteractionAction
import com.prod.match.domain.model.Recommendation

interface MatchGateway {
	suspend fun getRecommendations(): ApiOperationResult<List<Recommendation>>

	suspend fun createInteraction(
		candidateUserId: String,
		action: InteractionAction,
		mlRecommendationId: String?
	): ApiOperationResult<Interaction>
}
