package com.prod.auth.domain.usecase

import com.prod.auth.domain.gateway.AuthGateway
import com.prod.auth.domain.model.Profile
import com.prod.auth.domain.model.ProfileDraft
import com.prod.common.api.model.ApiOperationResult

class UpsertOwnProfileUseCase(
	private val authGateway: AuthGateway
) {
	suspend operator fun invoke(draft: ProfileDraft): ApiOperationResult<Profile> {
		return authGateway.upsertOwnProfile(draft)
	}
}
