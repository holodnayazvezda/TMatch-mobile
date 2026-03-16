package com.prod.chats.di

import com.prod.auth.domain.gateway.AuthSessionStore
import com.prod.chats.domain.usecase.EnsureChatStreamConnectedUseCase
import com.prod.chats.domain.usecase.ObserveChatMessagesUseCase
import com.prod.chats.domain.usecase.ObserveChatUseCase
import com.prod.chats.domain.usecase.ObserveOwnChatsUseCase
import com.prod.chats.domain.usecase.RefreshChatMessagesUseCase
import com.prod.chats.domain.usecase.RefreshOwnChatsUseCase
import com.prod.chats.domain.usecase.SendChatMessageUseCase
import com.prod.chats.presentation.ChatMessageUiMapper
import com.prod.chats.presentation.ChatPreviewUiMapper
import com.prod.chats.presentation.ChatViewModel
import com.prod.chats.presentation.ChatsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val chatsFeatureModule = module {
	single { ChatPreviewUiMapper() }
	single { ChatMessageUiMapper() }
	single { EnsureChatStreamConnectedUseCase(chatGateway = get()) }
	single { ObserveChatUseCase(chatGateway = get()) }
	single { ObserveChatMessagesUseCase(chatGateway = get()) }
	single { ObserveOwnChatsUseCase(chatGateway = get()) }
	single { RefreshChatMessagesUseCase(chatGateway = get()) }
	single { RefreshOwnChatsUseCase(chatGateway = get()) }
	single { SendChatMessageUseCase(chatGateway = get()) }

	viewModel {
		ChatsViewModel(
			observeOwnChatsUseCase = get(),
			refreshOwnChatsUseCase = get(),
			ensureChatStreamConnectedUseCase = get(),
			chatPreviewUiMapper = get()
		)
	}
	viewModel {
		ChatViewModel(
			authSessionStore = get<AuthSessionStore>(),
			observeChatUseCase = get(),
			observeChatMessagesUseCase = get(),
			refreshChatMessagesUseCase = get(),
			sendChatMessageUseCase = get(),
			ensureChatStreamConnectedUseCase = get(),
			chatMessageUiMapper = get()
		)
	}
}
