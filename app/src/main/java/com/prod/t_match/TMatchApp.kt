package com.prod.t_match

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.prod.auth.data.gateway.KtorAuthGateway
import com.prod.auth.presentation.flow.AuthFlowRoute
import com.prod.t_match.navigation.AppDestination
import com.prod.t_match.presentation.blank.BlankScreen
import com.prod.t_match.presentation.network.NoInternetScreen
import com.prod.ui.theme.TmatchTheme

@Composable
fun TMatchApp() {
	TmatchTheme {
		val authGateway = remember { KtorAuthGateway(baseUrl = BuildConfig.API_BASE_URL) }
		var destination by rememberSaveable { mutableStateOf(AppDestination.AUTH) }
		var noInternetRetryAction by remember { mutableStateOf<(() -> Unit)?>(null) }
		var isNoInternetVisible by remember { mutableStateOf(false) }

		Box(modifier = Modifier.fillMaxSize()) {
			when (destination) {
				AppDestination.AUTH -> AuthFlowRoute(
					authGateway = authGateway,
					onAuthSuccess = { destination = AppDestination.BLANK },
					onRequestNoInternet = { retryAction ->
						noInternetRetryAction = retryAction
						isNoInternetVisible = true
					}
				)

				AppDestination.BLANK -> BlankScreen()
			}

			if (isNoInternetVisible) {
				NoInternetScreen(
					onRetry = {
						isNoInternetVisible = false
						noInternetRetryAction?.invoke()
					}
				)
			}
		}
	}
}
