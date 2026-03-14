package com.prod.mathcing.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.prod.core.ui.theme.LocalDimensions
import com.prod.core.ui.theme.LocalShapes
import com.prod.mathcing.R
import com.prod.mathcing.model.MatchProfile
import com.prod.mathcing.model.MatchingContentController
import com.prod.ui.theme.LocalTmatchExtraColors
import com.prod.ui.theme.TmatchTheme
import kotlinx.coroutines.launch
import kotlin.math.abs

private enum class SwipeDirection {
	Left,
	Right
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainMatchingScreen(modifier: Modifier = Modifier) {
	Box(modifier = modifier.fillMaxSize())
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainMatchingScreenMock(modifier: Modifier = Modifier) {
	val dim = LocalDimensions.current
	val profiles = remember { MatchingContentController.profiles() }
	var currentIndex by rememberSaveable { mutableIntStateOf(0) }
	var isActionsSheetVisible by rememberSaveable { mutableStateOf(false) }
	val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
	val scope = rememberCoroutineScope()
	val currentProfile = profiles[currentIndex % profiles.size]

	Box(
		modifier = modifier
			.fillMaxSize()
	) {
		Column(
			modifier = Modifier
				.fillMaxSize()
				.padding(horizontal = dim.cardStackInset)
		) {
			MatchingHeader()
			Spacer(modifier = Modifier.height(dim.medium - dim.extraSmall))
			SwipeCardDeck(
				currentProfile = currentProfile,
				onMoreClick = {
					isActionsSheetVisible = true
				},
				onSwiped = {
					currentIndex = (currentIndex + 1) % profiles.size
				},
				modifier = Modifier.weight(1f)
			)
		}
		if (isActionsSheetVisible) {
			MatchingActionsBottomSheet(
				onDismiss = {
					scope.launch {
						sheetState.hide()
						isActionsSheetVisible = false
					}
				},
				sheetState = sheetState
			)
		}
	}
}

@Composable
private fun MatchingHeader() {
	val dim = LocalDimensions.current
	val colors = MaterialTheme.colorScheme
	Row(
		modifier = Modifier.fillMaxWidth(),
		verticalAlignment = Alignment.CenterVertically
	) {
		Spacer(modifier = Modifier.size(dim.iconContainer))
		Text(
			text = "Лента",
			modifier = Modifier
				.weight(1f),
			textAlign = TextAlign.Center,
			style = MaterialTheme.typography.titleLarge,
			color = colors.onBackground
		)
		Icon(
			painter = painterResource(R.drawable.ic_history_24),
			contentDescription = "История",
			modifier = Modifier.size(dim.iconMedium),
			tint = colors.onBackground
		)
	}
}

@Composable
private fun SwipeCardDeck(
	currentProfile: MatchProfile,
	onMoreClick: () -> Unit,
	onSwiped: (SwipeDirection) -> Unit,
	modifier: Modifier = Modifier
) {
	val dim = LocalDimensions.current
	val offsetX = remember(currentProfile.id) { Animatable(0f) }
	val rotation = remember(currentProfile.id) { Animatable(0f) }
	val scope = rememberCoroutineScope()

	Box(
		modifier = modifier.fillMaxWidth(),
		contentAlignment = Alignment.Center
	) {
		ProfileCard(
			profile = currentProfile,
			onMoreClick = onMoreClick,
			modifier = Modifier
				.fillMaxSize()
				.padding(horizontal = dim.extraSmall / 2)
				.graphicsLayer {
					translationX = offsetX.value
					rotationZ = rotation.value
				}
				.pointerInput(currentProfile.id) {
					detectDragGestures(
						onDrag = { change, dragAmount ->
							change.consume()
							scope.launch {
								offsetX.snapTo(offsetX.value + dragAmount.x)
								rotation.snapTo((offsetX.value / 45f).coerceIn(-12f, 12f))
							}
						},
						onDragEnd = {
							val target = offsetX.value
							val shouldDismiss = abs(target) > 240f
							scope.launch {
								if (shouldDismiss) {
									val direction =
										if (target >= 0f) SwipeDirection.Right else SwipeDirection.Left
									offsetX.animateTo(
										targetValue = if (direction == SwipeDirection.Right) 1200f else -1200f,
										animationSpec = tween(
											durationMillis = 220,
											easing = FastOutSlowInEasing
										)
									)
									onSwiped(direction)
								} else {
									offsetX.animateTo(
										0f,
										tween(durationMillis = 260, easing = FastOutSlowInEasing)
									)
									rotation.animateTo(
										0f,
										tween(durationMillis = 260, easing = FastOutSlowInEasing)
									)
								}
							}
						}
					)
				}
		)
	}
}

@Composable
private fun ProfileCard(
	profile: MatchProfile,
	modifier: Modifier = Modifier,
	onMoreClick: (() -> Unit)? = null
) {
	val dim = LocalDimensions.current
	val shapes = LocalShapes.current
	val extraColors = LocalTmatchExtraColors.current
	val typo = MaterialTheme.typography

	Surface(
		modifier = modifier,
		shape = RoundedCornerShape(shapes.veryBigRoundedValue),
		color = Color.White,
		shadowElevation = dim.cardStackInset
	) {
		Column(
			modifier = Modifier
				.fillMaxSize()
				.padding(dim.cardOuterPadding)
		) {
			Box(
				modifier = Modifier
					.fillMaxSize()
					.clip(RoundedCornerShape(shapes.veryBigRoundedValue - dim.cardOuterPadding))
			) {
				Image(
					painter = painterResource(profile.imageRes),
					contentDescription = "Фото ${profile.name}",
					contentScale = ContentScale.Crop,
					modifier = Modifier.matchParentSize()
				)
				if (onMoreClick != null) {
					IconBadge(
						iconRes = R.drawable.ic_menu_24,
						modifier = Modifier
							.padding(dim.medium - dim.extraSmall)
							.size(dim.iconContainer + dim.verySmall - dim.extraSmall)
							.align(Alignment.TopEnd)
							.clip(CircleShape)
							.clickable(onClick = onMoreClick)
					)
				}
				Column(
					modifier = Modifier
						.align(Alignment.BottomStart)
						.fillMaxWidth()
						.background(extraColors.baseScrim)
						.padding(dim.medium),
					verticalArrangement = Arrangement.spacedBy(dim.small - dim.extraSmall / 2)
				) {
					Text(
						text = "${profile.name}, ${profile.age}",
						style = typo.titleLarge,
						color = extraColors.onBaseScrim
					)
					Surface(
						shape = CircleShape,
						color = extraColors.accentGold
					) {
						Text(
							text = profile.matchLabel,
							modifier = Modifier.padding(
								horizontal = dim.small + dim.extraSmall / 2,
								vertical = dim.verySmall
							),
							style = typo.labelLarge,
							color = extraColors.onBaseScrim
						)
					}
				}
			}
		}
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MatchingActionsBottomSheet(
	onDismiss: () -> Unit,
	sheetState: SheetState
) {
	val dim = LocalDimensions.current
	val colors = MaterialTheme.colorScheme
	ModalBottomSheet(
		onDismissRequest = onDismiss,
		sheetState = sheetState,
		containerColor = colors.surface,
		contentColor = colors.onSurface,
		dragHandle = null
	) {
		Column(
			modifier = Modifier
				.fillMaxWidth()
				.padding(horizontal = dim.medium, vertical = dim.small),
			verticalArrangement = Arrangement.spacedBy(dim.small)
		) {
			Box(
				modifier = Modifier
					.align(Alignment.CenterHorizontally)
					.fillMaxWidth(0.18f)
					.height(dim.sheetHandleHeight)
					.clip(CircleShape)
					.background(colors.onSurface.copy(alpha = 0.28f))
			)
			ActionSheetButton(
				text = "Открыть профиль",
				onClick = onDismiss,
				accent = colors.primaryContainer,
				onAccent = colors.onPrimaryContainer
			)
			ActionSheetButton(
				text = "Заблокировать",
				onClick = onDismiss,
				accent = colors.errorContainer,
				onAccent = colors.onErrorContainer
			)
			ActionSheetButton(
				text = "Закрыть",
				onClick = onDismiss,
				accent = colors.tertiaryContainer,
				onAccent = colors.onTertiaryContainer
			)
		}
	}
}

@Composable
private fun ActionSheetButton(
	text: String,
	onClick: () -> Unit,
	accent: Color,
	onAccent: Color
) {
	val dim = LocalDimensions.current
	val shapes = LocalShapes.current
	Button(
		onClick = onClick,
		modifier = Modifier
			.fillMaxWidth()
			.heightIn(min = dim.actionButtonHeight),
		shape = shapes.mediumRounded,
		colors = ButtonDefaults.buttonColors(
			containerColor = accent.copy(alpha = 0.16f),
			contentColor = onAccent
		)
	) {
		Text(text = text, style = MaterialTheme.typography.titleMedium)
	}
}

@Composable
private fun IconBadge(
	iconRes: Int,
	modifier: Modifier = Modifier
) {
	val dim = LocalDimensions.current
	val shapes = LocalShapes.current
	val extraColors = LocalTmatchExtraColors.current

	Surface(
		modifier = modifier,
		shape = CircleShape,
		color = extraColors.baseScrim,
		border = BorderStroke(
			width = dim.extraSmall / 4,
			color = extraColors.onBaseScrim
		)
	) {
		Box(contentAlignment = Alignment.Center) {
			Icon(
				painter = painterResource(iconRes),
				contentDescription = null,
				modifier = Modifier.size(dim.iconSmall),
				tint = extraColors.onBaseScrim
			)
		}
	}
}

@Preview(showBackground = true, backgroundColor = 0xFF07090E, widthDp = 390, heightDp = 844)
@Composable
private fun MainMatchingScreenPreview() {
	TmatchTheme(darkTheme = true) {
		Scaffold {
			MainMatchingScreenMock(modifier = Modifier.padding(it))
		}
	}
}
