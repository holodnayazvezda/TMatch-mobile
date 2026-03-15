package com.prod.profile.domain.gateway

import com.prod.common.api.model.ApiOperationResult
import com.prod.profile.domain.model.DatingProfile
import com.prod.profile.domain.model.DatingProfileDraft

interface ProfileGateway {
	suspend fun getOwnDatingProfile(): ApiOperationResult<DatingProfile?>
	suspend fun upsertOwnDatingProfile(draft: DatingProfileDraft): ApiOperationResult<DatingProfile>
	suspend fun uploadDatingProfilePhoto(
		contentType: String,
		bytes: ByteArray
	): ApiOperationResult<String>
}
