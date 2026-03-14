package com.prod.auth.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data class OnboardingNavKey(
	val initialPage: Int = 0
): NavKey
