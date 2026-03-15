package com.prod.profile.domain.usecase

import com.prod.common.api.model.ApiOperationResult
import com.prod.profile.domain.gateway.ProfileGateway
import com.prod.profile.domain.model.DatingProfile

class GetOwnDatingProfileUseCase(
	private val profileGateway: ProfileGateway
) {
	suspend operator fun invoke(): ApiOperationResult<DatingProfile?> {
		return profileGateway.getOwnDatingProfile()
	}
}
