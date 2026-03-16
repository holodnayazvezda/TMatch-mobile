package com.prod.chats.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.EntryProviderScope
import coil3.compose.AsyncImage
import com.prod.chats.R
import com.prod.chats.presentation.ChatAccent
import com.prod.chats.presentation.ChatListItemUiState
import com.prod.chats.presentation.ChatsUiState
import com.prod.chats.presentation.ChatsViewModel
import com.prod.ui.components.AppScreenTopBar
import com.prod.ui.components.AppSectionCard
import com.prod.ui.components.buttons.AppMediumButton
import com.prod.ui.theme.LocalDimensions
import org.koin.androidx.compose.koinViewModel

fun EntryProviderScope<Any>.chatsGraph(
	onUnauthorized: () -> Unit,
	onRequestNoInternet: (retryAction: () -> Unit) -> Unit
) {
	entry<ChatsNavKey> {
		ChatsScreen(
			onUnauthorized = onUnauthorized,
			onRequestNoInternet = onRequestNoInternet
		)
	}
}

@Composable
private fun ChatsScreen(
	onUnauthorized: () -> Unit,
	onRequestNoInternet: (retryAction: () -> Unit) -> Unit,
	viewModel: ChatsViewModel = koinViewModel()
) {
	val uiState = viewModel.uiState

	LaunchedEffect(Unit) {
		viewModel.initialize(
			onUnauthorized = onUnauthorized,
			onNoInternet = onRequestNoInternet
		)
	}

	Column(
		modifier = Modifier
			.fillMaxSize()
			.safeDrawingPadding()
	) {
		AppScreenTopBar(
			title = stringResource(R.string.chats_screen_title),
			applySafeDrawingPadding = false
		)

		when {
			uiState.isLoading -> {
				Box(
					modifier = Modifier
						.fillMaxWidth()
						.weight(1f),
					contentAlignment = Alignment.Center
				) {
					CircularProgressIndicator()
				}
			}

			uiState.error != null -> {
				ChatsErrorState(
					message = uiState.error,
					onRetry = {
						viewModel.retry(
							onUnauthorized = onUnauthorized,
							onNoInternet = onRequestNoInternet
						)
					},
					modifier = Modifier
						.fillMaxWidth()
						.weight(1f)
						.padding(horizontal = LocalDimensions.current.large)
				)
			}

			uiState.items.isEmpty() -> {
				ChatsEmptyState(
					modifier = Modifier
						.fillMaxWidth()
						.weight(1f)
						.padding(horizontal = LocalDimensions.current.large)
				)
			}

			else -> {
				ChatsList(
					uiState = uiState,
					modifier = Modifier
						.fillMaxWidth()
						.weight(1f)
				)
			}
		}
	}
}

@Composable
private fun ChatsList(
	uiState: ChatsUiState,
	modifier: Modifier = Modifier
) {
	val dim = LocalDimensions.current

	LazyColumn(
		modifier = modifier,
		contentPadding = PaddingValues(
			start = dim.medium,
			top = dim.small,
			end = dim.medium,
			bottom = dim.large
		),
		verticalArrangement = Arrangement.spacedBy(dim.small)
	) {
		items(uiState.items, key = { it.chatId }) { item ->
			ChatRow(item = item)
		}
	}
}

@Composable
private fun ChatRow(
	item: ChatListItemUiState,
	modifier: Modifier = Modifier
) {
	val dim = LocalDimensions.current
	val colorScheme = MaterialTheme.colorScheme
	val (avatarColor, avatarContentColor) = when (item.accent) {
		ChatAccent.Primary -> colorScheme.primaryContainer to colorScheme.onPrimaryContainer
		ChatAccent.Secondary -> colorScheme.secondaryContainer to colorScheme.onSecondaryContainer
		ChatAccent.Tertiary -> colorScheme.tertiaryContainer to colorScheme.onTertiaryContainer
	}

	Surface(
		modifier = modifier.fillMaxWidth(),
		shape = MaterialTheme.shapes.extraLarge,
		color = colorScheme.surfaceContainerLow,
		tonalElevation = 2.dp
	) {
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.padding(horizontal = dim.defaultSpacing, vertical = dim.small),
			horizontalArrangement = Arrangement.spacedBy(dim.small),
			verticalAlignment = Alignment.CenterVertically
		) {
			Box(
				modifier = Modifier
					.size(52.dp)
					.clip(CircleShape)
					.background(avatarColor),
				contentAlignment = Alignment.Center
			) {
				if (item.avatarUrl != null) {
					AsyncImage(
						model = item.avatarUrl,
						contentDescription = item.title,
						modifier = Modifier.fillMaxSize(),
						contentScale = ContentScale.Crop
					)
				} else {
					Text(
						text = item.title.take(1).uppercase(),
						style = MaterialTheme.typography.titleMedium,
						color = avatarContentColor,
						fontWeight = FontWeight.Bold
					)
				}
			}

			Column(
				modifier = Modifier.weight(1f),
				verticalArrangement = Arrangement.spacedBy(4.dp)
			) {
				Text(
					text = item.title,
					style = MaterialTheme.typography.titleMedium,
					color = colorScheme.onSurface,
					maxLines = 1,
					overflow = TextOverflow.Ellipsis
				)
				Text(
					text = item.subtitle,
					style = MaterialTheme.typography.bodyMedium,
					color = colorScheme.onSurfaceVariant,
					maxLines = 2,
					overflow = TextOverflow.Ellipsis
				)
			}

			item.timestamp?.let { timestamp ->
				Text(
					text = timestamp,
					style = MaterialTheme.typography.labelMedium,
					color = colorScheme.onSurfaceVariant
				)
			}
		}
	}
}

@Composable
private fun ChatsEmptyState(
	modifier: Modifier = Modifier
) {
	Box(
		modifier = modifier,
		contentAlignment = Alignment.Center
	) {
		AppSectionCard {
			Text(
				text = stringResource(R.string.chats_empty_title),
				style = MaterialTheme.typography.titleMedium,
				color = MaterialTheme.colorScheme.onSurface,
				fontWeight = FontWeight.SemiBold
			)
			Text(
				text = stringResource(R.string.chats_empty_description),
				style = MaterialTheme.typography.bodyMedium,
				color = MaterialTheme.colorScheme.onSurfaceVariant
			)
		}
	}
}

@Composable
private fun ChatsErrorState(
	message: String,
	onRetry: () -> Unit,
	modifier: Modifier = Modifier
) {
	Box(
		modifier = modifier,
		contentAlignment = Alignment.Center
	) {
		AppSectionCard {
			Text(
				text = stringResource(R.string.chats_error_title),
				style = MaterialTheme.typography.titleMedium,
				color = MaterialTheme.colorScheme.onSurface,
				fontWeight = FontWeight.SemiBold
			)
			Text(
				text = message,
				style = MaterialTheme.typography.bodyMedium,
				color = MaterialTheme.colorScheme.onSurfaceVariant
			)
			Spacer(modifier = Modifier.height(4.dp))
			AppMediumButton(
				onClick = onRetry,
				buttonText = stringResource(R.string.chats_retry),
				isLoading = false
			)
		}
	}
}
