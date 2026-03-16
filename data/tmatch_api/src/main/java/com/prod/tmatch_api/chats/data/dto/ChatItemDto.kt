package com.prod.tmatch_api.chats.data.dto

import com.prod.tmatch_api.profile.data.dto.OwnCardDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChatItemDto(
	@SerialName("chat_id")
	val chatId: String,
	@SerialName("other_user_id")
	val otherUserId: String,
	@SerialName("last_message_id")
	val lastMessageId: String? = null,
	@SerialName("last_message_text")
	val lastMessageText: String? = null,
	@SerialName("last_message_created_at")
	val lastMessageCreatedAt: String? = null,
	@SerialName("other_user_card")
	val otherUserCard: OwnCardDto? = null
)
