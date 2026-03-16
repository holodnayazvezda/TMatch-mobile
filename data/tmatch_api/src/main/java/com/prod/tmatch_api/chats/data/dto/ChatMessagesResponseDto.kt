package com.prod.tmatch_api.chats.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class ChatMessagesResponseDto(
	val items: List<ChatMessageItemDto>
)
