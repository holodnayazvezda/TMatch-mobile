package com.prod.t_match.navigation

import com.prod.auth.domain.gateway.AuthSessionStore
import com.prod.auth.navigation.OnboardingNavKey
import com.prod.auth.navigation.ProfileSetupNavKey
import com.prod.common.api.model.ApiErrorReason
import com.prod.common.api.model.ApiOperationResult
import com.prod.profile.domain.gateway.ProfileGateway
import com.prod.profile.navigation.ProfileNavKey

class AppStartDestinationResolver(
	private val authSessionStore: AuthSessionStore,
	private val profileGateway: ProfileGateway,
	private val clearSessionState: suspend () -> Unit
) {
	suspend fun resolve(): Any {
		if (authSessionStore.get() == null) {
			return OnboardingNavKey()
		}

		return resolveAuthenticatedDestination()
	}

	suspend fun resolveAuthenticatedDestination(): Any {
		return when (val result = profileGateway.getOwnCard()) {
			is ApiOperationResult.Success -> {
				val isProfileCompleted = result.data.hasAccountProfile
				val hasDatingProfile = result.data.hasDatingProfile
				authSessionStore.setProfileCompleted(isProfileCompleted)
				authSessionStore.setHasDatingProfile(hasDatingProfile)
				if (isProfileCompleted) {
					ProfileNavKey(source = "startup_resolver")
				} else {
					authSessionStore.setHasDatingProfile(false)
					ProfileSetupNavKey()
				}
			}

			is ApiOperationResult.ApiError -> {
				if (result.reason == ApiErrorReason.Unauthorized) {
					resetSessionState()
					OnboardingNavKey()
				} else {
					fallbackDestination()
				}
			}

			is ApiOperationResult.ValidationError,
			ApiOperationResult.NoInternet -> fallbackDestination()
		}
	}

	private fun fallbackDestination(): Any {
		return if (authSessionStore.isProfileCompleted()) {
			ProfileNavKey(source = "startup_fallback")
		} else {
			authSessionStore.setHasDatingProfile(false)
			ProfileSetupNavKey()
		}
	}

	private suspend fun resetSessionState() {
		try {
			clearSessionState()
		} finally {
			authSessionStore.clear()
		}
	}
}
