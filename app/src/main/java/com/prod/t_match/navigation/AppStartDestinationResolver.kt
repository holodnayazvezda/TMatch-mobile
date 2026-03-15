package com.prod.t_match.navigation

import com.prod.auth.domain.gateway.AuthGateway
import com.prod.auth.domain.gateway.AuthSessionStore
import com.prod.auth.navigation.OnboardingNavKey
import com.prod.auth.navigation.ProfileSetupNavKey
import com.prod.common.api.model.ApiErrorReason
import com.prod.common.api.model.ApiOperationResult
import com.prod.profile.navigation.ProfileNavKey

class AppStartDestinationResolver(
	private val authSessionStore: AuthSessionStore,
	private val authGateway: AuthGateway
) {
	suspend fun resolve(): Any {
		if (authSessionStore.get() == null) {
			return OnboardingNavKey()
		}

		return when (val result = authGateway.getOwnProfile()) {
			is ApiOperationResult.Success -> {
				val isProfileCompleted = result.data != null
				authSessionStore.setProfileCompleted(isProfileCompleted)
				if (isProfileCompleted) {
					ProfileNavKey(source = "startup_resolver")
				} else {
					ProfileSetupNavKey()
				}
			}

			is ApiOperationResult.ApiError -> {
				if (result.reason == ApiErrorReason.Unauthorized) {
					authSessionStore.clear()
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
			ProfileSetupNavKey()
		}
	}
}
