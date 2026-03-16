package com.prod.matching.domain.usecase

import com.prod.match.domain.gateway.MatchGateway
import com.prod.match.domain.model.TargetingPreferencesDraft

class UpsertOwnTargetingUseCase(
	private val matchGateway: MatchGateway
) {
	suspend operator fun invoke(draft: TargetingPreferencesDraft) =
		matchGateway.upsertOwnTargeting(draft)
}
