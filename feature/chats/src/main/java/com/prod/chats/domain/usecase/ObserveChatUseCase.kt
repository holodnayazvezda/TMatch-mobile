package com.prod.chats.domain.usecase

import com.prod.chats.domain.gateway.ChatGateway
import com.prod.chats.domain.model.ChatPreview
import kotlinx.coroutines.flow.Flow

class ObserveChatUseCase(
	private val chatGateway: ChatGateway
) {
	operator fun invoke(chatId: String): Flow<ChatPreview?> {
		return chatGateway.observeChat(chatId)
	}
}
