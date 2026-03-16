package com.prod.chats.presentation

import com.prod.chats.domain.model.ChatMessage
import com.prod.chats.domain.model.ChatPreview
import java.time.OffsetDateTime
import java.time.format.DateTimeParseException

class ChatMessageUiMapper {
	fun mapMessage(
		message: ChatMessage,
		currentUserId: String?
	): ChatMessageUiState {
		return ChatMessageUiState(
			messageId = message.messageId,
			text = message.text,
			timestamp = formatTimestamp(message.createdAt),
			isOwn = currentUserId != null && currentUserId == message.senderUserId
		)
	}

	fun resolveTitle(chat: ChatPreview?): String {
		val profile = chat?.otherUserCard?.profile
		val fullName = listOfNotNull(
			profile?.firstName?.takeIf { it.isNotBlank() },
			profile?.lastName?.takeIf { it.isNotBlank() }
		).joinToString(" ")
		return fullName.ifBlank { "Чат" }
	}

	private fun formatTimestamp(value: String): String {
		val timestamp = try {
			OffsetDateTime.parse(value)
		} catch (_: DateTimeParseException) {
			return value
		}
		val hour = timestamp.hour.toString().padStart(2, '0')
		val minute = timestamp.minute.toString().padStart(2, '0')
		return "$hour:$minute"
	}
}
