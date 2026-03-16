package com.prod.chats.di

import com.prod.chats.domain.usecase.GetOwnChatsUseCase
import com.prod.chats.presentation.ChatPreviewUiMapper
import com.prod.chats.presentation.ChatsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val chatsFeatureModule = module {
	single { ChatPreviewUiMapper() }
	single { GetOwnChatsUseCase(chatGateway = get()) }

	viewModel {
		ChatsViewModel(
			getOwnChatsUseCase = get(),
			chatPreviewUiMapper = get()
		)
	}
}
