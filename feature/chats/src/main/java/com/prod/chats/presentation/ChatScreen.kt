package com.prod.chats.presentation

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imeNestedScroll
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
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
import androidx.compose.ui.unit.sp
import com.prod.chats.R
import com.prod.ui.components.AppBackButton
import com.prod.ui.components.AppInfoChip
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
	var isCompanionIdeasDialogVisible by rememberSaveable(chatId) { mutableStateOf(false) }

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
			otherUserGender = uiState.otherUserGender,
			onBack = onBack,
			onOpenProfile = onOpenProfile,
			onOpenCompanionIdeas = { isCompanionIdeasDialogVisible = true }
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

				uiState.messages.isEmpty() -> {
					ChatEmptyConversationHint(
						modifier = Modifier
							.align(Alignment.Center)
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

	if (isCompanionIdeasDialogVisible) {
		CompanionIdeasDialog(
			gender = uiState.otherUserGender,
			onDismiss = { isCompanionIdeasDialogVisible = false }
		)
	}
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChatHeader(
	title: String,
	avatarUrl: String?,
	otherUserId: String?,
	otherUserGender: String?,
	onBack: () -> Unit,
	onOpenProfile: (userId: String) -> Unit,
	onOpenCompanionIdeas: () -> Unit,
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
				style = MaterialTheme.typography.titleMedium.copy(fontSize = 20.sp),
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
					.widthIn(max = 240.dp)
					.height(48.dp)
					.align(Alignment.Center)
					.then(
						if (otherUserId != null) {
							Modifier.combinedClickable(
								interactionSource = headerInteractionSource,
								indication = null,
								onClickLabel = stringResource(R.string.chat_header_open_profile),
								onLongClickLabel = stringResource(R.string.chat_header_open_assistant),
								onClick = { onOpenProfile(otherUserId) },
								onLongClick = { onOpenCompanionIdeas() }
							)
						} else {
							Modifier
						}
					)
			)
		}
	}
}

@Composable
private fun ChatEmptyConversationHint(
	modifier: Modifier = Modifier
) {
	AppSectionCard(
		modifier = modifier.widthIn(max = 340.dp),
		shape = RoundedCornerShape(28.dp),
		containerColor = MaterialTheme.colorScheme.surfaceContainerLow
	) {
		Column(
			verticalArrangement = Arrangement.spacedBy(12.dp),
			horizontalAlignment = Alignment.CenterHorizontally
		) {
			Text(
				text = stringResource(R.string.chat_empty_hint_title),
				style = MaterialTheme.typography.titleMedium,
				fontWeight = FontWeight.SemiBold,
				color = MaterialTheme.colorScheme.onSurface,
				textAlign = TextAlign.Center
			)
			Text(
				text = stringResource(R.string.chat_empty_hint_description),
				style = MaterialTheme.typography.bodyMedium,
				color = MaterialTheme.colorScheme.onSurfaceVariant,
				textAlign = TextAlign.Center
			)
		}
	}
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CompanionIdeasDialog(
	gender: String?,
	onDismiss: () -> Unit
) {
	val suggestions = companionIdeaOptions(gender)
	val descriptionRes = when (gender?.lowercase()) {
		"female" -> R.string.chat_assistant_dialog_description_female
		"male" -> R.string.chat_assistant_dialog_description_male
		else -> R.string.chat_assistant_dialog_description_unknown
	}

	AlertDialog(
		onDismissRequest = onDismiss,
		title = {
			Text(
				text = stringResource(R.string.chat_assistant_dialog_title),
				style = MaterialTheme.typography.titleMedium,
				fontWeight = FontWeight.SemiBold
			)
		},
		text = {
			Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
				Text(
					text = stringResource(descriptionRes),
					style = MaterialTheme.typography.bodyMedium,
					color = MaterialTheme.colorScheme.onSurfaceVariant
				)
				FlowRow(
					horizontalArrangement = Arrangement.spacedBy(8.dp),
					verticalArrangement = Arrangement.spacedBy(8.dp)
				) {
					suggestions.forEach { suggestionRes ->
						AppInfoChip(
							text = stringResource(suggestionRes),
							containerColor = MaterialTheme.colorScheme.secondaryContainer,
							contentColor = MaterialTheme.colorScheme.onSecondaryContainer
						)
					}
				}
			}
		},
		confirmButton = {
			Button(
				onClick = onDismiss,
				colors = ButtonDefaults.buttonColors(
					containerColor = MaterialTheme.colorScheme.primary,
					contentColor = MaterialTheme.colorScheme.onPrimary
				)
			) {
				Text(text = stringResource(R.string.chat_assistant_back))
			}
		}
	)
}

private fun companionIdeaOptions(gender: String?): List<Int> {
	return when (gender?.lowercase()) {
		"female" -> listOf(
			R.string.chat_assistant_female_option_flowers,
			R.string.chat_assistant_female_option_sweets,
			R.string.chat_assistant_female_option_coffee
		)

		"male" -> listOf(
			R.string.chat_assistant_male_option_dinner,
			R.string.chat_assistant_male_option_barber,
			R.string.chat_assistant_male_option_activity
		)

		else -> listOf(
			R.string.chat_assistant_unknown_option_walk,
			R.string.chat_assistant_unknown_option_coffee,
			R.string.chat_assistant_unknown_option_gift
		)
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
