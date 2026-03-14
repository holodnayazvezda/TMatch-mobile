package com.prod.t_match.di

import com.prod.auth.di.authApiModule
import com.prod.auth.di.authFeatureModule
import com.prod.auth.presentation.signin.SignInViewModel
import com.prod.auth.presentation.signup.SignUpViewModel
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin

class AppModulesSmokeTest {

	@After
	fun tearDown() {
		stopKoin()
	}

	@Test
	fun authViewModelsResolveFromKoin() {
		val koin = startKoin {
			modules(
				appModule,
				authApiModule,
				authFeatureModule
			)
		}.koin

		assertNotNull(koin.get<SignInViewModel>())
		assertNotNull(koin.get<SignUpViewModel>())
	}
}
