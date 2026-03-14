package com.prod.tmatch_api.di

import com.prod.auth.domain.gateway.AuthGateway
import com.prod.tmatch_api.auth.data.gateway.KtorAuthGateway
import org.koin.dsl.module

fun tmatchApiDataModule(
	baseUrl: String
) = module {
	single<AuthGateway> {
		KtorAuthGateway(baseUrl = baseUrl)
	}
}
