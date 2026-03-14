package com.prod.auth.domain.usecase

import com.prod.auth.domain.gateway.AuthGateway
import com.prod.auth.domain.model.AuthOperationResult

class SignInWithEmailUseCase(
	private val authGateway: AuthGateway
) {
	suspend operator fun invoke(email: String, password: String): AuthOperationResult {
		return authGateway.signInWithEmail(
			email = email,
			password = password
		)
	}
}
