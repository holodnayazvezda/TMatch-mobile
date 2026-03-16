package com.prod.profile.domain.usecase

import com.prod.common.api.model.ApiOperationResult
import com.prod.profile.domain.gateway.ProfileGateway
import com.prod.profile.domain.model.UserCard

class GetUserCardUseCase(
	private val profileGateway: ProfileGateway
) {
	suspend operator fun invoke(userId: String): ApiOperationResult<UserCard> {
		return profileGateway.getUserCard(userId)
	}
}
