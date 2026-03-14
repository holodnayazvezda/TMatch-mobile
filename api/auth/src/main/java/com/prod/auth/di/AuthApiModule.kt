package com.prod.auth.di

import com.prod.auth.data.gateway.KtorAuthGateway
import com.prod.auth.domain.gateway.AuthGateway
import org.koin.core.qualifier.named
import org.koin.dsl.module

val authApiModule = module {
	single<AuthGateway> {
		KtorAuthGateway(baseUrl = get(named(AuthQualifiers.API_BASE_URL)))
	}
}
