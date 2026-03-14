package com.prod.t_match.navigation.model

import com.prod.auth.presentation.flow.model.AuthNavKey
import com.prod.auth.presentation.flow.model.OnboardingNavKey
import kotlinx.serialization.Serializable

@Serializable
data class AuthFlowNavKey(
	val initialDestination: AuthNavKey = OnboardingNavKey()
) : AppNavKey
