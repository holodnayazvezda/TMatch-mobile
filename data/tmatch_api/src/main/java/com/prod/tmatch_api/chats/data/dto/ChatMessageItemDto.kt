package com.prod.tmatch_api.chats.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChatMessageItemDto(
	@SerialName("message_id")
	val messageId: String,
	@SerialName("chat_id")
	val chatId: String,
	@SerialName("sender_user_id")
	val senderUserId: String,
	val text: String,
	@SerialName("created_at")
	val createdAt: String
)
