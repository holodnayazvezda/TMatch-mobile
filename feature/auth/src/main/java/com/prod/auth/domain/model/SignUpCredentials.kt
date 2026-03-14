package com.prod.auth.domain.model

data class SignUpCredentials(
	val email: String,
	val password: String,
	val confirmPassword: String
)
