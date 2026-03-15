package com.prod.matching.domain.usecase

import com.prod.matching.domain.model.MatchAction

class SubmitMatchActionUseCase {
	suspend operator fun invoke(
		profileId: String,
		action: MatchAction
	) {
		TODO("Implement match action submission when API/domain contracts are ready")
	}
}
