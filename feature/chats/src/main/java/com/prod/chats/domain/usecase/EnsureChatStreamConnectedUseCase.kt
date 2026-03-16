package com.prod.chats.domain.usecase

import com.prod.chats.domain.gateway.ChatGateway
import com.prod.common.api.model.ApiOperationResult

class EnsureChatStreamConnectedUseCase(
	private val chatGateway: ChatGateway
) {
	suspend operator fun invoke(): ApiOperationResult<Unit> {
		return chatGateway.ensureChatStreamConnected()
	}
}
