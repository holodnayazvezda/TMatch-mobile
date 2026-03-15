package com.prod.match.domain.model

data class RecommendationCandidateProfile(
	val firstName: String,
	val lastName: String?,
	val birthDate: String,
	val region: String?,
	val avatarUrl: String?
)
