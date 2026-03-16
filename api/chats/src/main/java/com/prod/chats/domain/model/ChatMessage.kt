package com.prod.chats.domain.model

data class ChatMessage(
	val messageId: String,
	val chatId: String,
	val senderUserId: String,
	val text: String,
	val createdAt: String
)
