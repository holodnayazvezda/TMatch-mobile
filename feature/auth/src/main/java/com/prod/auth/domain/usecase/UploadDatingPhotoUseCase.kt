package com.prod.auth.domain.usecase

import com.prod.common.api.model.ApiOperationResult
import com.prod.profile.domain.gateway.ProfileGateway

class UploadDatingPhotoUseCase(
	private val profileGateway: ProfileGateway
) {
	suspend operator fun invoke(
		contentType: String,
		bytes: ByteArray
	): ApiOperationResult<String> {
		return profileGateway.uploadDatingProfilePhoto(
			contentType = contentType,
			bytes = bytes
		)
	}
}
