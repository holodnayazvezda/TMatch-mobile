package com.prod.matching.domain.usecase

import com.prod.match.domain.gateway.MatchGateway

class GetOwnTargetingUseCase(
	private val matchGateway: MatchGateway
) {
	suspend operator fun invoke() = matchGateway.getOwnTargeting()
}
