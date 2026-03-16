package com.prod.chats.domain.usecase

import com.prod.chats.domain.gateway.ChatGateway
import com.prod.chats.domain.model.ChatPreview
import kotlinx.coroutines.flow.Flow

class ObserveOwnChatsUseCase(
	private val chatGateway: ChatGateway
) {
	operator fun invoke(): Flow<List<ChatPreview>> {
		return chatGateway.observeOwnChats()
	}
}
