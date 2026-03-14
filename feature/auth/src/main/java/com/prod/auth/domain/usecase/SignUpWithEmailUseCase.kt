package com.prod.auth.domain.usecase

import com.prod.auth.domain.gateway.AuthGateway
import com.prod.auth.domain.model.AuthOperationResult

class SignUpWithEmailUseCase(
	private val authGateway: AuthGateway
) {
	suspend operator fun invoke(email: String, password: String): AuthOperationResult {
		return authGateway.signUpWithEmail(
			email = email,
			password = password
		)
	}
}
