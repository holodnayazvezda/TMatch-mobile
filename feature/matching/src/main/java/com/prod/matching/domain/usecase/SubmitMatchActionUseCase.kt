package com.prod.matching.domain.usecase

import com.prod.common.api.model.ApiOperationResult
import com.prod.match.domain.gateway.MatchGateway
import com.prod.match.domain.model.Interaction
import com.prod.match.domain.model.InteractionAction
import com.prod.matching.domain.model.MatchAction

class SubmitMatchActionUseCase(
	private val matchGateway: MatchGateway
) {
	suspend operator fun invoke(
		profileId: String,
		action: MatchAction,
		recommendationId: String?
	): ApiOperationResult<Interaction>? {
		val interactionAction = when (action) {
			MatchAction.Like -> InteractionAction.Like
			MatchAction.Dislike -> InteractionAction.Dislike
			MatchAction.Ban -> InteractionAction.Block
			MatchAction.Skip -> return null
		}

		return matchGateway.createInteraction(
			candidateUserId = profileId,
			action = interactionAction,
			mlRecommendationId = recommendationId
		)
	}
}
