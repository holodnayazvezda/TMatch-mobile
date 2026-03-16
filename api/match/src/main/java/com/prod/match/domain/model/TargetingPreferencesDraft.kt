package com.prod.match.domain.model

data class TargetingPreferencesDraft(
	val region: String?,
	val genderTarget: TargetGender,
	val ageFrom: Int,
	val ageTo: Int
)
