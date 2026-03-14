package com.prod.auth.presentation.flow.model

import kotlinx.serialization.Serializable

@Serializable
data class OnboardingNavKey(
	val initialPage: Int = 0
) : AuthNavKey
