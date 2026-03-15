package com.prod.tmatch_api.di

import com.prod.auth.domain.gateway.AuthSessionStore
import com.prod.auth.domain.gateway.AuthGateway
import com.prod.tmatch_api.auth.data.gateway.KtorAuthGateway
import com.prod.tmatch_api.auth.data.gateway.SharedPrefsAuthSessionStore
import com.prod.tmatch_api.auth.data.network.createAuthHttpClient
import org.koin.dsl.module

fun tmatchApiDataModule(
	baseUrl: String
) = module {
	single { createAuthHttpClient(baseUrl = baseUrl) }
	single<AuthSessionStore> {
		SharedPrefsAuthSessionStore(context = get())
	}
	single<AuthGateway> {
		KtorAuthGateway(
			httpClient = get(),
			authSessionStore = get()
		)
	}
}
