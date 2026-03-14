package com.prod.auth.presentation.flow.model

import kotlinx.serialization.Serializable

@Serializable
data class SignUpNavKey(
	val prefilledEmail: String = ""
) : AuthNavKey
