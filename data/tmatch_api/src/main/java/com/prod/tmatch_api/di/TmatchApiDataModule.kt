package com.prod.tmatch_api.di

import com.prod.auth.domain.gateway.AuthSessionStore
import com.prod.auth.domain.gateway.AuthGateway
import com.prod.match.domain.gateway.MatchGateway
import com.prod.profile.domain.gateway.ProfileGateway
import com.prod.tmatch_api.auth.data.gateway.KtorAuthGateway
import com.prod.tmatch_api.auth.data.gateway.SharedPrefsAuthSessionStore
import com.prod.tmatch_api.match.data.gateway.KtorMatchGateway
import com.prod.tmatch_api.auth.data.network.createAuthHttpClient
import com.prod.tmatch_api.profile.data.gateway.KtorProfileGateway
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
	single<ProfileGateway> {
		KtorProfileGateway(
			httpClient = get(),
			authSessionStore = get()
		)
	}
	single<MatchGateway> {
		KtorMatchGateway(
			httpClient = get(),
			authSessionStore = get()
		)
	}
}
