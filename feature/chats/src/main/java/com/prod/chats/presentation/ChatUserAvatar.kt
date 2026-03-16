package com.prod.chats.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import coil3.compose.AsyncImage

@Composable
fun ChatUserAvatar(
	title: String,
	avatarUrl: String?,
	accent: ChatAccent,
	size: Dp,
	modifier: Modifier = Modifier
) {
	val colorScheme = MaterialTheme.colorScheme
	val (avatarColor, avatarContentColor) = when (accent) {
		ChatAccent.Primary -> colorScheme.primaryContainer to colorScheme.onPrimaryContainer
		ChatAccent.Secondary -> colorScheme.secondaryContainer to colorScheme.onSecondaryContainer
		ChatAccent.Tertiary -> colorScheme.tertiaryContainer to colorScheme.onTertiaryContainer
	}

	Box(
		modifier = modifier
			.size(size)
			.clip(CircleShape)
			.background(avatarColor),
		contentAlignment = Alignment.Center
	) {
		if (avatarUrl != null) {
			AsyncImage(
				model = avatarUrl,
				contentDescription = title,
				modifier = Modifier.fillMaxSize(),
				contentScale = ContentScale.Crop
			)
		} else {
			Text(
				text = title.take(1).uppercase(),
				style = MaterialTheme.typography.titleMedium,
				color = avatarContentColor,
				fontWeight = FontWeight.Bold
			)
		}
	}
}

fun chatAccentForSeed(seed: String?): ChatAccent {
	if (seed.isNullOrBlank()) return ChatAccent.Primary
	return when (seed.sumOf(Char::code) % 3) {
		0 -> ChatAccent.Primary
		1 -> ChatAccent.Secondary
		else -> ChatAccent.Tertiary
	}
}
