package com.prod.tmatch_api.chats.data.local

import androidx.room.Entity

@Entity(
	tableName = "chat_previews",
	primaryKeys = ["ownerUserId", "chatId"]
)
data class ChatPreviewEntity(
	val ownerUserId: String,
	val chatId: String,
	val otherUserId: String,
	val lastMessageId: String?,
	val lastMessageText: String?,
	val lastMessageCreatedAt: String?,
	val otherProfileFirstName: String?,
	val otherProfileLastName: String?,
	val otherProfileBirthDate: String?,
	val otherProfileGender: String?,
	val otherProfileRegion: String?,
	val otherProfileAvatarUrl: String?,
	val otherDatingPhotos: String
)
