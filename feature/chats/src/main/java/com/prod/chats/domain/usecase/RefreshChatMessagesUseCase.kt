package com.prod.chats.domain.usecase

import com.prod.chats.domain.gateway.ChatGateway
import com.prod.chats.domain.model.ChatMessage
import com.prod.common.api.model.ApiOperationResult

class RefreshChatMessagesUseCase(
	private val chatGateway: ChatGateway
) {
	suspend operator fun invoke(
		chatId: String,
		limit: Int,
		beforeMessageId: String? = null
	): ApiOperationResult<List<ChatMessage>> {
		return chatGateway.refreshChatMessages(
			chatId = chatId,
			limit = limit,
			beforeMessageId = beforeMessageId
		)
	}
}
