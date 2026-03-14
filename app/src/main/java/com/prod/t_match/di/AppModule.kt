package com.prod.t_match.di

import com.prod.auth.di.AuthQualifiers
import com.prod.t_match.BuildConfig
import com.prod.t_match.presentation.blank.BlankViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

val appModule = module {
	single(named(AuthQualifiers.API_BASE_URL)) { BuildConfig.API_BASE_URL }
	viewModel { BlankViewModel() }
}
