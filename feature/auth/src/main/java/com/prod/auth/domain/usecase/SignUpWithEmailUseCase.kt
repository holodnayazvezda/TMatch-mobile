package com.prod.auth.domain.usecase

import com.prod.auth.domain.gateway.AuthGateway
import com.prod.common.api.model.ApiOperationResult
import com.prod.common.api.model.UserSession

class SignUpWithEmailUseCase(
	private val authGateway: AuthGateway
) {
	suspend operator fun invoke(email: String, password: String): ApiOperationResult<UserSession> {
		return authGateway.signUpWithEmail(
			email = email,
			password = password
		)
	}
}
