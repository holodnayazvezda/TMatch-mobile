package com.prod.auth.domain.usecase

import com.prod.auth.domain.gateway.AuthGateway
import com.prod.auth.domain.model.Profile
import com.prod.common.api.model.ApiOperationResult

class GetOwnProfileUseCase(
	private val authGateway: AuthGateway
) {
	suspend operator fun invoke(): ApiOperationResult<Profile?> {
		return authGateway.getOwnProfile()
	}
}
