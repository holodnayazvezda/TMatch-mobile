package com.prod.chats.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imeNestedScroll
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.prod.chats.R
import com.prod.ui.components.AppBackButton
import com.prod.ui.components.AppSectionCard
import com.prod.ui.components.buttons.AppMediumButton
import com.prod.ui.components.text_fields.AppTextField
import com.prod.ui.theme.LocalDimensions
import kotlinx.coroutines.flow.distinctUntilChanged
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChatScreen(
	chatId: String,
	onBack: () -> Unit,
	onOpenProfile: (userId: String) -> Unit,
	onUnauthorized: () -> Unit,
	onRequestNoInternet: (retryAction: () -> Unit) -> Unit,
	viewModel: ChatViewModel = koinViewModel()
) {
	val uiState = viewModel.uiState
	val listState = rememberLazyListState()
	val density = LocalDensity.current
	val imeBottomPadding = WindowInsets.ime.asPaddingValues().calculateBottomPadding()

	var composerHeightPx by remember { mutableIntStateOf(0) }
	val composerHeightDp = with(density) { composerHeightPx.toDp() }

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

	LaunchedEffect(imeBottomPadding, uiState.messages.size) {
		if (imeBottomPadding > 0.dp && uiState.messages.isNotEmpty()) {
			listState.animateScrollToItem(uiState.messages.lastIndex)
		}
	}

	Column(
		modifier = Modifier
			.fillMaxSize()
			.imeNestedScroll()
	) {
		ChatHeader(
			title = uiState.title,
			avatarUrl = uiState.avatarUrl,
			otherUserId = uiState.otherUserId,
			onBack = onBack,
			onOpenProfile = onOpenProfile
		)

		Box(
			modifier = Modifier
				.fillMaxWidth()
				.weight(1f)
		) {
			when {
				uiState.isLoading && uiState.messages.isEmpty() -> {
					Box(
						modifier = Modifier.fillMaxSize(),
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
							.fillMaxSize()
							.padding(horizontal = LocalDimensions.current.large)
					)
				}

				else -> {
					ChatMessagesList(
						uiState = uiState,
						listState = listState,
						bottomContentPadding = composerHeightDp,
						modifier = Modifier.fillMaxSize()
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
				modifier = Modifier
					.align(Alignment.BottomCenter)
					.fillMaxWidth()
					.onSizeChanged { composerHeightPx = it.height }
					.imePadding()
					.navigationBarsPadding()
			)
		}
	}
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChatHeader(
	title: String,
	avatarUrl: String?,
	otherUserId: String?,
	onBack: () -> Unit,
	onOpenProfile: (userId: String) -> Unit,
	modifier: Modifier = Modifier
) {
	val dim = LocalDimensions.current
	val headerInteractionSource = remember { MutableInteractionSource() }

	Row(
		modifier = modifier
			.fillMaxWidth()
			.padding(horizontal = dim.small, vertical = dim.verySmall),
		verticalAlignment = Alignment.CenterVertically
	) {
		Box(
			modifier = Modifier.fillMaxWidth()
		) {
			AppBackButton(
				text = stringResource(R.string.chat_back),
				onClick = onBack,
				modifier = Modifier
					.align(Alignment.CenterStart)
					.padding(start = 4.dp)
			)
			Text(
				text = title,
				modifier = Modifier
					.align(Alignment.Center)
					.padding(horizontal = 124.dp),
				style = MaterialTheme.typography.titleLarge,
				color = MaterialTheme.colorScheme.onBackground,
				maxLines = 1,
				overflow = TextOverflow.Ellipsis,
				textAlign = TextAlign.Center
			)
			ChatHeaderAvatar(
				title = title,
				avatarUrl = avatarUrl,
				otherUserId = otherUserId,
				modifier = Modifier.align(Alignment.CenterEnd)
			)
			Box(
				modifier = Modifier
					.fillMaxWidth()
					.height(48.dp)
					.padding(start = 124.dp)
					.then(
						if (otherUserId != null) {
							Modifier.clickable(
								interactionSource = headerInteractionSource,
								indication = null
							) {
								onOpenProfile(otherUserId)
							}
						} else {
							Modifier
						}
					)
			)
		}
	}
}

@Composable
private fun ChatHeaderAvatar(
	title: String,
	avatarUrl: String?,
	otherUserId: String?,
	modifier: Modifier = Modifier
) {
	ChatUserAvatar(
		title = title,
		avatarUrl = avatarUrl,
		accent = chatAccentForSeed(otherUserId),
		size = 36.dp,
		modifier = modifier
	)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChatMessagesList(
	uiState: ChatUiState,
	listState: LazyListState,
	bottomContentPadding: androidx.compose.ui.unit.Dp,
	modifier: Modifier = Modifier
) {
	val dim = LocalDimensions.current

	LazyColumn(
		state = listState,
		modifier = modifier,
		contentPadding = PaddingValues(
			start = dim.medium,
			top = dim.medium,
			end = dim.medium,
			bottom = bottomContentPadding + dim.medium
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
			modifier = Modifier
				.fillMaxWidth(0.82f)
				.widthIn(max = 420.dp),
			shape = MaterialTheme.shapes.large,
			color = containerColor,
			contentColor = contentColor,
			tonalElevation = if (message.isOwn) 2.dp else 0.dp,
			shadowElevation = if (message.isOwn) 0.dp else 1.dp
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
	val dim = LocalDimensions.current
	val colorScheme = MaterialTheme.colorScheme

	Surface(
		modifier = modifier
			.fillMaxWidth()
			.padding(horizontal = dim.medium, vertical = dim.small),
		color = MaterialTheme.colorScheme.surface,
		shape = MaterialTheme.shapes.extraLarge,
		tonalElevation = 4.dp,
		shadowElevation = 4.dp
	) {
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.padding(dim.small),
			horizontalArrangement = Arrangement.spacedBy(dim.small),
			verticalAlignment = Alignment.Top
		) {
			AppTextField(
				modifier = Modifier.weight(1f),
				value = value,
				onValueChange = onValueChange,
				label = stringResource(R.string.chat_input_placeholder),
				focusedPlaceholder = stringResource(R.string.chat_input_placeholder),
				singleLine = false,
				maxLines = 6,
				minHeight = 56.dp,
				keyboardOptions = KeyboardOptions(imeAction = ImeAction.Default)
			)

			FilledIconButton(
				onClick = onSend,
				colors = IconButtonDefaults.filledIconButtonColors(
					containerColor = colorScheme.primary,
					contentColor = colorScheme.onPrimary
				),
				modifier = Modifier
					.requiredSize(56.dp)
					.clip(CircleShape)
			) {
				if (isSending) {
					CircularProgressIndicator(
						modifier = Modifier.size(20.dp),
						strokeWidth = 2.dp
					)
				} else {
					Icon(
						painter = painterResource(R.drawable.ic_send_24),
						contentDescription = stringResource(R.string.chat_send_content_description),
						tint = colorScheme.onPrimary
					)
				}
			}
		}
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
