package com.prod.auth.domain.model

data class AuthSession(
	val accessToken: String,
	val userId: String
)
