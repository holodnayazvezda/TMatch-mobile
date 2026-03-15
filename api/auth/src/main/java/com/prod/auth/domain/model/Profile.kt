package com.prod.auth.domain.model

data class Profile(
	val userId: String,
	val firstName: String,
	val lastName: String?,
	val birthDate: String,
	val gender: Gender,
	val region: String?,
	val avatarUrl: String?
)
