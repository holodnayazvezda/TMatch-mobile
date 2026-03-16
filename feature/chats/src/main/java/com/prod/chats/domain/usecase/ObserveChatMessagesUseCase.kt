package com.prod.chats.domain.usecase

import com.prod.chats.domain.gateway.ChatGateway
import com.prod.chats.domain.model.ChatMessage
import kotlinx.coroutines.flow.Flow

class ObserveChatMessagesUseCase(
	private val chatGateway: ChatGateway
) {
	operator fun invoke(chatId: String): Flow<List<ChatMessage>> {
		return chatGateway.observeChatMessages(chatId)
	}
}
