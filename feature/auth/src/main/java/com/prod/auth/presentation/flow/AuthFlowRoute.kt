package com.prod.auth.presentation.flow

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.prod.auth.domain.gateway.AuthGateway
import com.prod.auth.domain.model.AuthSession
import com.prod.auth.presentation.onboarding.OnboardingScreen
import com.prod.auth.presentation.signin.SignInScreen
import com.prod.auth.presentation.signup.SignUpScreen

@Composable
fun AuthFlowRoute(
	authGateway: AuthGateway,
	onAuthSuccess: (AuthSession) -> Unit,
	onRequestNoInternet: (retryAction: () -> Unit) -> Unit,
	modifier: Modifier = Modifier
) {
	var destination by rememberSaveable { mutableStateOf(AuthDestination.ONBOARDING) }

	when (destination) {
		AuthDestination.ONBOARDING -> OnboardingScreen(
			modifier = modifier,
			onCreateAccount = { AuthDestination.SIGN_UP },
			onLogin = { AuthDestination.SIGN_IN }
		)

		AuthDestination.SIGN_IN -> SignInScreen(
			modifier = modifier,
			authGateway = authGateway,
			onBack = { AuthDestination.ONBOARDING },
			onSwitchToSignUp = { AuthDestination.SIGN_UP },
			onAuthSuccess = onAuthSuccess,
			onRequestNoInternet = onRequestNoInternet
		)

		AuthDestination.SIGN_UP -> SignUpScreen(
			modifier = modifier,
			authGateway = authGateway,
			onBack = { AuthDestination.ONBOARDING },
			onSwitchToSignIn = { AuthDestination.SIGN_IN },
			onAuthSuccess = onAuthSuccess,
			onRequestNoInternet = onRequestNoInternet
		)
	}
}
