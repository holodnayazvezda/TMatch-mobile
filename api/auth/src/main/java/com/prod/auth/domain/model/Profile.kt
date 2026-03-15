package com.prod.auth.domain.model

data class Profile(
	val userId: String,
	val firstName: String,
	val lastName: String?,
	val birthDate: String,
	val region: String?,
	val avatarUrl: String?
)
