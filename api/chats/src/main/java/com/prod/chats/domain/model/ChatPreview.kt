package com.prod.chats.domain.model

import com.prod.profile.domain.model.UserCard

data class ChatPreview(
	val chatId: String,
	val otherUserId: String,
	val lastMessageId: String?,
	val lastMessageText: String?,
	val lastMessageCreatedAt: String?,
	val otherUserCard: UserCard?
)
