package com.prod.match.domain.gateway

import com.prod.common.api.model.ApiOperationResult
import com.prod.match.domain.model.IncomingLike
import com.prod.match.domain.model.Interaction
import com.prod.match.domain.model.InteractionAction
import com.prod.match.domain.model.Recommendation
import com.prod.match.domain.model.TargetingPreferences
import com.prod.match.domain.model.TargetingPreferencesDraft

interface MatchGateway {
	suspend fun getIncomingLikes(): ApiOperationResult<List<IncomingLike>>

	suspend fun getRecommendations(): ApiOperationResult<List<Recommendation>>

	suspend fun getOwnTargeting(): ApiOperationResult<TargetingPreferences>

	suspend fun upsertOwnTargeting(
		draft: TargetingPreferencesDraft
	): ApiOperationResult<TargetingPreferences>

	suspend fun createInteraction(
		candidateUserId: String,
		action: InteractionAction,
		mlRecommendationId: String?
	): ApiOperationResult<Interaction>
}
