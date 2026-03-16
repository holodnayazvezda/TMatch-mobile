package com.prod.chats.domain.gateway

import com.prod.chats.domain.model.ChatMessage
import com.prod.chats.domain.model.ChatPreview
import com.prod.common.api.model.ApiOperationResult
import kotlinx.coroutines.flow.Flow

interface ChatGateway {
	fun observeOwnChats(): Flow<List<ChatPreview>>

	fun observeChat(chatId: String): Flow<ChatPreview?>

	fun observeChatMessages(chatId: String): Flow<List<ChatMessage>>

	suspend fun refreshOwnChats(): ApiOperationResult<Unit>

	suspend fun refreshChatMessages(
		chatId: String,
		limit: Int,
		beforeMessageId: String? = null
	): ApiOperationResult<List<ChatMessage>>

	suspend fun sendMessage(
		chatId: String,
		text: String
	): ApiOperationResult<ChatMessage>

	suspend fun ensureChatStreamConnected(): ApiOperationResult<Unit>

	suspend fun clearLocalData()
}
