package com.prod.chats.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.text.style.TextAlign
import com.prod.chats.R
import com.prod.ui.components.AppScreenTopBar
import com.prod.ui.components.AppSectionCard
import com.prod.ui.components.buttons.AppMediumButton
import com.prod.ui.components.text_fields.AppTextField
import com.prod.ui.theme.LocalDimensions
import kotlinx.coroutines.flow.distinctUntilChanged
import org.koin.androidx.compose.koinViewModel
import androidx.compose.ui.res.stringResource

@Composable
fun ChatScreen(
	chatId: String,
	onBack: () -> Unit,
	onUnauthorized: () -> Unit,
	onRequestNoInternet: (retryAction: () -> Unit) -> Unit,
	viewModel: ChatViewModel = koinViewModel()
) {
	val uiState = viewModel.uiState
	val listState = rememberLazyListState()

	LaunchedEffect(chatId) {
		viewModel.initialize(
			chatId = chatId,
			onUnauthorized = onUnauthorized,
			onNoInternet = onRequestNoInternet
		)
	}

	LaunchedEffect(chatId, listState) {
		snapshotFlow {
			listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0
		}
			.distinctUntilChanged()
			.collect { isAtTop ->
				if (isAtTop) {
					viewModel.loadOlderMessages(
						chatId = chatId,
						onUnauthorized = onUnauthorized,
						onNoInternet = onRequestNoInternet
					)
				}
			}
	}

	val lastMessageId = uiState.messages.lastOrNull()?.messageId
	LaunchedEffect(lastMessageId, uiState.isLoadingMore) {
		if (lastMessageId != null && !uiState.isLoadingMore) {
			listState.animateScrollToItem(uiState.messages.lastIndex)
		}
	}

	Column(
		modifier = Modifier
			.fillMaxSize()
			.safeDrawingPadding()
			.imePadding()
	) {
		AppScreenTopBar(
			title = uiState.title,
			applySafeDrawingPadding = false,
			navigation = {
				Text(
					text = stringResource(R.string.chat_back),
					modifier = Modifier.clickable(onClick = onBack),
					style = MaterialTheme.typography.labelLarge,
					color = MaterialTheme.colorScheme.primary
				)
			}
		)

		when {
			uiState.isLoading && uiState.messages.isEmpty() -> {
				Box(
					modifier = Modifier
						.fillMaxWidth()
						.weight(1f),
					contentAlignment = Alignment.Center
				) {
					CircularProgressIndicator()
				}
			}

			uiState.error != null && uiState.messages.isEmpty() -> {
					ChatErrorState(
						message = uiState.error,
						onRetry = {
							viewModel.retry(
								chatId = chatId,
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

			else -> {
				ChatMessagesList(
					uiState = uiState,
					listState = listState,
					modifier = Modifier
						.fillMaxWidth()
						.weight(1f)
				)
			}
		}

		ChatComposer(
			value = uiState.draftMessage,
			onValueChange = viewModel::onDraftChanged,
			isSending = uiState.isSending,
			onSend = {
				viewModel.sendMessage(
					chatId = chatId,
					onUnauthorized = onUnauthorized,
					onNoInternet = onRequestNoInternet
				)
			},
			modifier = Modifier.padding(horizontal = LocalDimensions.current.medium)
		)
	}
}

@Composable
private fun ChatMessagesList(
	uiState: ChatUiState,
	listState: androidx.compose.foundation.lazy.LazyListState,
	modifier: Modifier = Modifier
) {
	val dim = LocalDimensions.current

	LazyColumn(
		state = listState,
		modifier = modifier,
		contentPadding = PaddingValues(
			start = dim.medium,
			top = dim.small,
			end = dim.medium,
			bottom = dim.medium
		),
		verticalArrangement = Arrangement.spacedBy(dim.small)
	) {
		if (uiState.isLoadingMore) {
			item("loading_more") {
				Box(
					modifier = Modifier.fillMaxWidth(),
					contentAlignment = Alignment.Center
				) {
					CircularProgressIndicator()
				}
			}
		}

		items(uiState.messages, key = { it.messageId }) { message ->
			ChatMessageBubble(
				message = message,
				modifier = Modifier.fillMaxWidth()
			)
		}
	}
}

@Composable
private fun ChatMessageBubble(
	message: ChatMessageUiState,
	modifier: Modifier = Modifier
) {
	val colorScheme = MaterialTheme.colorScheme
	val alignment = if (message.isOwn) Alignment.CenterEnd else Alignment.CenterStart
	val containerColor = if (message.isOwn) {
		colorScheme.primaryContainer
	} else {
		colorScheme.surfaceContainerLow
	}
	val contentColor = if (message.isOwn) {
		colorScheme.onPrimaryContainer
	} else {
		colorScheme.onSurface
	}

	Box(
		modifier = modifier,
		contentAlignment = alignment
	) {
		Surface(
			shape = MaterialTheme.shapes.large,
			color = containerColor,
			contentColor = contentColor
		) {
			Column(
				modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
				verticalArrangement = Arrangement.spacedBy(6.dp)
			) {
				Text(text = message.text, style = MaterialTheme.typography.bodyLarge)
				Text(
					text = message.timestamp,
					style = MaterialTheme.typography.labelSmall,
					textAlign = TextAlign.End,
					modifier = Modifier.fillMaxWidth()
				)
			}
		}
	}
}

@Composable
private fun ChatComposer(
	value: String,
	onValueChange: (String) -> Unit,
	isSending: Boolean,
	onSend: () -> Unit,
	modifier: Modifier = Modifier
) {
	Column(
		modifier = modifier.padding(bottom = LocalDimensions.current.medium),
		verticalArrangement = Arrangement.spacedBy(LocalDimensions.current.small)
	) {
		AppTextField(
			value = value,
			onValueChange = onValueChange,
			label = stringResource(R.string.chat_input_placeholder),
			focusedPlaceholder = stringResource(R.string.chat_input_placeholder),
			singleLine = false,
			maxLines = 4,
			onDone = onSend
		)
		AppMediumButton(
			onClick = onSend,
			buttonText = stringResource(R.string.chat_send),
			isLoading = isSending
		)
	}
}

@Composable
private fun ChatErrorState(
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
				text = stringResource(R.string.chat_error_title),
				style = MaterialTheme.typography.titleMedium,
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
