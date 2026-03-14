package com.prod.auth.presentation.flow.model

import kotlinx.serialization.Serializable

@Serializable
data class SignInNavKey(
	val prefilledEmail: String = ""
) : AuthNavKey
