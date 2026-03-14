package com.prod.auth.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data class SignUpNavKey(
	val prefilledEmail: String = ""
): NavKey
