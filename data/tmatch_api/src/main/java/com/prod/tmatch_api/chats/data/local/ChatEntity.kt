package com.prod.tmatch_api.chats.data.local

import androidx.room.Entity

@Entity(
	tableName = "chats",
	primaryKeys = ["ownerUserId", "chatId"]
)
data class ChatEntity(
	val ownerUserId: String,
	val chatId: String,
	val otherUserId: String,
	val lastMessageId: String?,
	val lastMessageText: String?,
	val lastMessageCreatedAt: String?
)
