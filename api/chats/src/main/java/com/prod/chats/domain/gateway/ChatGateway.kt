package com.prod.chats.domain.gateway

import com.prod.chats.domain.model.ChatPreview
import com.prod.common.api.model.ApiOperationResult

interface ChatGateway {
	suspend fun getOwnChats(): ApiOperationResult<List<ChatPreview>>
}
