package com.prod.match.domain.model

data class TargetingPreferences(
	val region: String?,
	val genderTarget: TargetGender,
	val ageFrom: Int,
	val ageTo: Int
)
