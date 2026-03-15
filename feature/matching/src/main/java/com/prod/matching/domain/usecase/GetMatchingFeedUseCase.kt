package com.prod.matching.domain.usecase

import com.prod.common.api.model.ApiOperationResult
import com.prod.match.domain.gateway.MatchGateway
import com.prod.match.domain.model.Recommendation

class GetMatchingFeedUseCase(
	private val matchGateway: MatchGateway
) {
	suspend operator fun invoke(): ApiOperationResult<List<Recommendation>> {
		return matchGateway.getRecommendations()
	}
}
