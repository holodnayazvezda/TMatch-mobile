package com.prod.auth.domain.gateway

import com.prod.common.api.model.ApiOperationResult
import com.prod.common.api.model.UserSession
import com.prod.auth.domain.model.Profile
import com.prod.auth.domain.model.ProfileDraft

interface AuthGateway {
	suspend fun signInWithEmail(email: String, password: String): ApiOperationResult<UserSession>
	suspend fun signUpWithEmail(email: String, password: String): ApiOperationResult<UserSession>
	suspend fun getOwnProfile(): ApiOperationResult<Profile?>
	suspend fun upsertOwnProfile(draft: ProfileDraft): ApiOperationResult<Profile>
	suspend fun uploadDatingProfilePhoto(
		contentType: String,
		bytes: ByteArray
	): ApiOperationResult<String>
}
