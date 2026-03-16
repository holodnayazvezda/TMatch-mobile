package com.prod.tmatch_api.chats.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class ChatsResponseDto(
	val items: List<ChatItemDto>
)
