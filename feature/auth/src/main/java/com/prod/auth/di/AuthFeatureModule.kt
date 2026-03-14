package com.prod.auth.di

import com.prod.auth.domain.usecase.SignInWithEmailUseCase
import com.prod.auth.domain.usecase.SignUpWithEmailUseCase
import com.prod.auth.domain.validation.EmailValidator
import com.prod.auth.domain.validation.PasswordValidator
import com.prod.auth.presentation.onboarding.OnboardingViewModel
import com.prod.auth.presentation.signin.SignInViewModel
import com.prod.auth.presentation.signup.SignUpViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val authFeatureModule = module {
	single { EmailValidator() }
	single { PasswordValidator() }
	single { SignInWithEmailUseCase(authGateway = get()) }
	single { SignUpWithEmailUseCase(authGateway = get()) }

	viewModel { OnboardingViewModel() }
	viewModel {
		SignInViewModel(
			signInWithEmailUseCase = get(),
			emailValidator = get(),
			passwordValidator = get()
		)
	}
	viewModel {
		SignUpViewModel(
			signUpWithEmailUseCase = get(),
			emailValidator = get(),
			passwordValidator = get()
		)
	}
}
