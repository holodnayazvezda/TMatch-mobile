package com.prod.tmatch_api.di

import androidx.room.Room
import com.prod.auth.domain.gateway.AuthSessionStore
import com.prod.auth.domain.gateway.AuthGateway
import com.prod.chats.domain.gateway.ChatGateway
import com.prod.common.api.gateway.NotificationGateway
import com.prod.match.domain.gateway.MatchGateway
import com.prod.profile.domain.gateway.ProfileGateway
import com.prod.tmatch_api.auth.data.gateway.KtorAuthGateway
import com.prod.tmatch_api.auth.data.gateway.SharedPrefsAuthSessionStore
import com.prod.tmatch_api.chats.data.gateway.KtorChatGateway
import com.prod.tmatch_api.chats.data.local.TmatchChatsDatabase
import com.prod.tmatch_api.match.data.gateway.KtorMatchGateway
import com.prod.tmatch_api.notifications.data.gateway.KtorNotificationGateway
import com.prod.tmatch_api.auth.data.network.createAuthHttpClient
import com.prod.tmatch_api.common.data.network.ApiResponseOverrideRegistry
import com.prod.tmatch_api.common.data.network.InMemoryApiResponseOverrideRegistry
import com.prod.tmatch_api.profile.data.gateway.KtorProfileGateway
import org.koin.dsl.module

fun tmatchApiDataModule(
	baseUrl: String,
	enableResponseOverrides: Boolean = false
) = module {
	single<ApiResponseOverrideRegistry> { InMemoryApiResponseOverrideRegistry() }
	single {
		createAuthHttpClient(
			baseUrl = baseUrl,
			responseOverrideRegistry = get(),
			enableOverrides = enableResponseOverrides
		)
	}
	single<AuthSessionStore> {
		SharedPrefsAuthSessionStore(context = get())
	}
	single {
		Room.databaseBuilder(
			get(),
			TmatchChatsDatabase::class.java,
			"tmatch_chats.db"
		).build()
	}
	single { get<TmatchChatsDatabase>().chatPreviewDao() }
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
	single<ChatGateway> {
		KtorChatGateway(
			httpClient = get(),
			authSessionStore = get(),
			chatPreviewDao = get()
		)
	}
	single<MatchGateway> {
		KtorMatchGateway(
			httpClient = get(),
			authSessionStore = get()
		)
	}
	single<NotificationGateway> {
		KtorNotificationGateway(
			httpClient = get(),
			authSessionStore = get()
		)
	}
}
