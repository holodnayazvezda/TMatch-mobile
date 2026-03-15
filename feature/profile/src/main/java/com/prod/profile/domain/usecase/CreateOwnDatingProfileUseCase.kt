package com.prod.profile.domain.usecase

import com.prod.common.api.model.ApiOperationResult
import com.prod.profile.domain.model.DatingProfile
import com.prod.profile.domain.model.DatingProfileDraft
import com.prod.profile.domain.usecase.UpsertOwnDatingProfileUseCase

class CreateOwnDatingProfileUseCase(
	private val upsertOwnDatingProfileUseCase: UpsertOwnDatingProfileUseCase
) {
	suspend operator fun invoke(photoUrl: String): ApiOperationResult<DatingProfile> {
		return upsertOwnDatingProfileUseCase(
			DatingProfileDraft(
				photos = listOf(photoUrl)
			)
		)
	}
}
