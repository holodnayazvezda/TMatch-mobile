package com.prod.tmatch_api.chats.data.local

import androidx.room.Entity
import androidx.room.Index

@Entity(
	tableName = "chat_messages",
	primaryKeys = ["ownerUserId", "messageId"],
	indices = [
		Index(value = ["ownerUserId", "chatId", "createdAt"]),
		Index(value = ["ownerUserId", "chatId", "messageId"])
	]
)
data class ChatMessageEntity(
	val ownerUserId: String,
	val messageId: String,
	val chatId: String,
	val senderUserId: String,
	val text: String,
	val createdAt: String
)
