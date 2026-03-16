package com.prod.matching.domain.usecase

import com.prod.common.api.model.ApiOperationResult
import com.prod.match.domain.gateway.MatchGateway
import com.prod.matching.domain.model.MatchingFeed

class GetMatchingFeedUseCase(
	private val matchGateway: MatchGateway
) {
	suspend operator fun invoke(): ApiOperationResult<MatchingFeed> {
		return when (val incomingLikesResult = matchGateway.getIncomingLikes()) {
			is ApiOperationResult.Success -> {
				when (val recommendationsResult = matchGateway.getRecommendations()) {
					is ApiOperationResult.Success -> {
						ApiOperationResult.Success(
							MatchingFeed(
								incomingLikes = incomingLikesResult.data,
								recommendations = recommendationsResult.data
							)
						)
					}

					is ApiOperationResult.ValidationError -> recommendationsResult
					is ApiOperationResult.ApiError -> recommendationsResult
					ApiOperationResult.NoInternet -> ApiOperationResult.NoInternet
				}
			}

			is ApiOperationResult.ValidationError -> incomingLikesResult
			is ApiOperationResult.ApiError -> incomingLikesResult
			ApiOperationResult.NoInternet -> ApiOperationResult.NoInternet
		}
	}
}
