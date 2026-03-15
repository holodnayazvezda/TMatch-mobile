package com.prod.auth.domain.usecase

import com.prod.auth.domain.gateway.AuthGateway
import com.prod.common.api.model.ApiOperationResult

class UploadDatingPhotoUseCase(
	private val authGateway: AuthGateway
) {
	suspend operator fun invoke(
		contentType: String,
		bytes: ByteArray
	): ApiOperationResult<String> {
		return authGateway.uploadDatingProfilePhoto(
			contentType = contentType,
			bytes = bytes
		)
	}
}
