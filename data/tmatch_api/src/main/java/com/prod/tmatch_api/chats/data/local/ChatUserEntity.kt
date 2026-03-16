package com.prod.tmatch_api.chats.data.local

import androidx.room.Entity

@Entity(
	tableName = "chat_users",
	primaryKeys = ["ownerUserId", "userId"]
)
data class ChatUserEntity(
	val ownerUserId: String,
	val userId: String,
	val firstName: String?,
	val lastName: String?,
	val birthDate: String?,
	val gender: String?,
	val region: String?,
	val avatarUrl: String?,
	val datingPhotos: String
)
