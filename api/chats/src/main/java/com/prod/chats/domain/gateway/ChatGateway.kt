package com.prod.chats.domain.gateway

import com.prod.chats.domain.model.ChatPreview
import com.prod.common.api.model.ApiOperationResult
import kotlinx.coroutines.flow.Flow

interface ChatGateway {
	fun observeOwnChats(): Flow<List<ChatPreview>>

	suspend fun refreshOwnChats(): ApiOperationResult<Unit>
}
