package com.prod.auth.domain.usecase

import com.prod.auth.domain.model.Gender
import com.prod.auth.domain.model.ProfileDraft

class CreateJuryProfileDraftUseCase {
	operator fun invoke(): ProfileDraft {
		return ProfileDraft(
			firstName = "Алина",
			lastName = "Морозова",
			birthDate = "2001-04-18",
			gender = Gender.Female,
			region = "Москва",
			avatarUrl = null
		)
	}
}
