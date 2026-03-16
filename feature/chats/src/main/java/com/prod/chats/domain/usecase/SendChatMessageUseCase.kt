package com.prod.chats.domain.usecase

import com.prod.chats.domain.gateway.ChatGateway
import com.prod.chats.domain.model.ChatMessage
import com.prod.common.api.model.ApiOperationResult

class SendChatMessageUseCase(
	private val chatGateway: ChatGateway
) {
	suspend operator fun invoke(
		chatId: String,
		text: String
	): ApiOperationResult<ChatMessage> {
		return chatGateway.sendMessage(chatId, text)
	}
}
