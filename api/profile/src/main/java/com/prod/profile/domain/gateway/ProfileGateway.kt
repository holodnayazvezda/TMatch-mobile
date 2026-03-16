package com.prod.profile.domain.gateway

import com.prod.common.api.model.ApiOperationResult
import com.prod.profile.domain.model.DatingProfile
import com.prod.profile.domain.model.DatingProfileDraft
import com.prod.profile.domain.model.OwnCard
import com.prod.profile.domain.model.UserCard

interface ProfileGateway {
	suspend fun getOwnCard(): ApiOperationResult<OwnCard>
	suspend fun getUserCard(userId: String): ApiOperationResult<UserCard>
	suspend fun getOwnDatingProfile(): ApiOperationResult<DatingProfile?>
	suspend fun upsertOwnDatingProfile(draft: DatingProfileDraft): ApiOperationResult<DatingProfile>
	suspend fun uploadDatingProfilePhoto(
		contentType: String,
		bytes: ByteArray
	): ApiOperationResult<String>
}
