package com.prod.profile.domain.usecase

import com.prod.auth.domain.gateway.AuthGateway
import com.prod.auth.domain.model.Profile
import com.prod.common.api.model.ApiOperationResult

class GetOwnAccountProfileUseCase(
	private val authGateway: AuthGateway
) {
	suspend operator fun invoke(): ApiOperationResult<Profile?> {
		return authGateway.getOwnProfile()
	}
}
