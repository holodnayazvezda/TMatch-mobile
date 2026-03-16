package com.prod.chats.domain.usecase

import com.prod.chats.domain.gateway.ChatGateway
import com.prod.chats.domain.model.ChatPreview
import com.prod.common.api.model.ApiOperationResult

class GetOwnChatsUseCase(
	private val chatGateway: ChatGateway
) {
	suspend operator fun invoke(): ApiOperationResult<List<ChatPreview>> {
		return chatGateway.getOwnChats()
	}
}
