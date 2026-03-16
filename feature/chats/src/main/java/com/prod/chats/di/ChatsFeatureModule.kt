package com.prod.chats.di

import com.prod.chats.domain.usecase.ObserveOwnChatsUseCase
import com.prod.chats.domain.usecase.RefreshOwnChatsUseCase
import com.prod.chats.presentation.ChatPreviewUiMapper
import com.prod.chats.presentation.ChatsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val chatsFeatureModule = module {
	single { ChatPreviewUiMapper() }
	single { ObserveOwnChatsUseCase(chatGateway = get()) }
	single { RefreshOwnChatsUseCase(chatGateway = get()) }

	viewModel {
		ChatsViewModel(
			observeOwnChatsUseCase = get(),
			refreshOwnChatsUseCase = get(),
			chatPreviewUiMapper = get()
		)
	}
}
