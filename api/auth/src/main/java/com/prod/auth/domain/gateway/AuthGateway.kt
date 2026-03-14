package com.prod.auth.domain.gateway

import com.prod.auth.domain.model.AuthOperationResult

interface AuthGateway {
	suspend fun signInWithEmail(email: String, password: String): AuthOperationResult
	suspend fun signUpWithEmail(email: String, password: String): AuthOperationResult
}
