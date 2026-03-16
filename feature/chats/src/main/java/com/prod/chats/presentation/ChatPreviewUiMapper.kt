package com.prod.chats.presentation

import com.prod.chats.domain.model.ChatPreview
import java.time.OffsetDateTime
import java.time.format.DateTimeParseException
import java.util.Locale
import java.time.format.TextStyle

class ChatPreviewUiMapper {
	fun map(items: List<ChatPreview>): List<ChatListItemUiState> {
		return items
			.sortedWith(
				compareByDescending<ChatPreview> { it.lastMessageCreatedAt != null }
					.thenByDescending { parseTimestamp(it.lastMessageCreatedAt) }
					.thenBy { it.otherUserId }
			)
			.map { chat ->
				val profile = chat.otherUserCard?.profile
				val fullName = listOfNotNull(
					profile?.firstName?.takeIf { it.isNotBlank() },
					profile?.lastName?.takeIf { it.isNotBlank() }
				).joinToString(" ")
				ChatListItemUiState(
					chatId = chat.chatId,
					title = fullName.ifBlank { "ID ${chat.otherUserId.take(8)}" },
					subtitle = chat.lastMessageText?.takeIf { it.isNotBlank() }
						?: "Сообщений пока нет",
					timestamp = chat.lastMessageCreatedAt?.let(::formatTimestamp),
					accent = chat.otherUserId.toAccent(),
					avatarUrl = profile?.avatarUrl
						?: chat.otherUserCard?.datingProfile?.photos?.firstOrNull()
				)
			}
	}

	private fun parseTimestamp(value: String?): OffsetDateTime? {
		if (value == null) return null
		return try {
			OffsetDateTime.parse(value)
		} catch (_: DateTimeParseException) {
			null
		}
	}

	private fun formatTimestamp(value: String): String {
		val timestamp = parseTimestamp(value) ?: return value
		val day = timestamp.dayOfMonth.toString().padStart(2, '0')
		val month = timestamp.month.getDisplayName(TextStyle.SHORT, Locale.forLanguageTag("ru"))
			.replaceFirstChar { char -> char.lowercase() }
		val hour = timestamp.hour.toString().padStart(2, '0')
		val minute = timestamp.minute.toString().padStart(2, '0')
		return "$day $month, $hour:$minute"
	}

	private fun String.toAccent(): ChatAccent {
		return when (sumOf(Char::code) % 3) {
			0 -> ChatAccent.Primary
			1 -> ChatAccent.Secondary
			else -> ChatAccent.Tertiary
		}
	}
}
