package com.prod.auth.domain.usecase

import com.prod.auth.domain.gateway.AuthGateway
import com.prod.auth.domain.model.JuryPreviewUser
import com.prod.common.api.model.ApiOperationResult

class GetJuryPreviewUsersUseCase(
	private val authGateway: AuthGateway
) {
	suspend operator fun invoke(): ApiOperationResult<List<JuryPreviewUser>> {
		return authGateway.getJuryPreviewUsers()
	}
}
