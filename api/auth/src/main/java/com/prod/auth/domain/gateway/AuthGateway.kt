package com.prod.auth.domain.gateway

import com.prod.common.api.model.ApiOperationResult
import com.prod.common.api.model.UserSession

interface AuthGateway {
	suspend fun signInWithEmail(email: String, password: String): ApiOperationResult<UserSession>
	suspend fun signUpWithEmail(email: String, password: String): ApiOperationResult<UserSession>
}
