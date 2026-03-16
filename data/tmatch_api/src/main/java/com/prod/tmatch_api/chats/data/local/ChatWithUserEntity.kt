package com.prod.tmatch_api.chats.data.local

import androidx.room.Embedded
import androidx.room.Relation

data class ChatWithUserEntity(
	@Embedded
	val chat: ChatEntity,
	@Relation(
		parentColumn = "otherUserId",
		entityColumn = "userId"
	)
	val user: ChatUserEntity?
)
