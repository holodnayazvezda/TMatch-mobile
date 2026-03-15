package com.prod.profile.domain.usecase

import com.prod.common.api.model.ApiOperationResult
import com.prod.profile.domain.gateway.ProfileGateway
import com.prod.profile.domain.model.DatingProfile
import com.prod.profile.domain.model.DatingProfileDraft

class UpsertOwnDatingProfileUseCase(
	private val profileGateway: ProfileGateway
) {
	suspend operator fun invoke(draft: DatingProfileDraft): ApiOperationResult<DatingProfile> {
		return profileGateway.upsertOwnDatingProfile(draft)
	}
}
