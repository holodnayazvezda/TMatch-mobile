package com.prod.mathcing.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.runtime.mutableFloatStateOf
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
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlin.math.abs

private enum class SwipeDirection {
	Left,
	Right,
	Up
}

private const val SwipeDismissThreshold = 240f
private const val SwipeRotationDivisor = 45f
private const val SwipeRotationLimit = 12f
private const val SwipeDismissOffset = 1200f
private const val NextCardMinAlpha = 0.1f
private const val UpSwipeScaleDelta = 0.06f
private const val UpSwipeAlphaDelta = 0.18f
private const val SwipeOffscreenMargin = 240f
private const val SwipeDismissDurationMillis = 260
private const val UpSwipeDismissDurationMillis = 320

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainMatchingScreen(modifier: Modifier = Modifier) {
	Box(modifier = modifier.fillMaxSize())
}

@OptIn(ExperimentalMaterial3Api::class)
private interface MainMatchingContentController {
	val currentProfile: MatchProfile
	val nextProfile: MatchProfile?
	val isActionsSheetVisible: Boolean
	val sheetState: SheetState

	fun onMoreClick()
	fun onSwiped(direction: SwipeDirection)
	fun onDismissSheet()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainMatchingContent(
	controller: MainMatchingContentController,
	modifier: Modifier = Modifier
) {
	val dim = LocalDimensions.current

	Box(
		modifier = modifier.fillMaxSize()
	) {
		Column(
			modifier = Modifier
				.fillMaxSize()
				.padding(horizontal = dim.cardStackInset)
		) {
			MatchingHeader()
			Spacer(modifier = Modifier.height(dim.medium - dim.extraSmall))
			SwipeCardDeck(
				currentProfile = controller.currentProfile,
				nextProfile = controller.nextProfile,
				onMoreClick = controller::onMoreClick,
				onSwiped = controller::onSwiped,
				modifier = Modifier.weight(1f)
			)
		}
		if (controller.isActionsSheetVisible) {
			MatchingActionsBottomSheet(
				onDismiss = controller::onDismissSheet,
				sheetState = controller.sheetState
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
	nextProfile: MatchProfile?,
	onMoreClick: () -> Unit,
	onSwiped: (SwipeDirection) -> Unit,
	modifier: Modifier = Modifier
) {
	val dim = LocalDimensions.current
	var offsetX by remember(currentProfile.id) { mutableFloatStateOf(0f) }
	var offsetY by remember(currentProfile.id) { mutableFloatStateOf(0f) }
	var rotation by remember(currentProfile.id) { mutableFloatStateOf(0f) }
	val scope = rememberCoroutineScope()
	BoxWithConstraints(
		modifier = modifier.fillMaxWidth(),
		contentAlignment = Alignment.Center
	) {
		val verticalDismissOffset = constraints.maxHeight.toFloat() + SwipeOffscreenMargin
		val swipeDistance = maxOf(abs(offsetX), abs(offsetY))
		val swipeProgress = (swipeDistance / SwipeDismissOffset).coerceIn(0f, 1f)
		val nextCardAlpha = NextCardMinAlpha + (1f - NextCardMinAlpha) * swipeProgress
		val upSwipeProgress = (-offsetY / verticalDismissOffset).coerceIn(0f, 1f)
		val currentCardScale = 1f - UpSwipeScaleDelta * upSwipeProgress
		val currentCardAlpha = 1f - UpSwipeAlphaDelta * upSwipeProgress
		val resetSpec = tween<Float>(
			durationMillis = SwipeDismissDurationMillis,
			easing = FastOutSlowInEasing
		)

		if (nextProfile != null) {
			ProfileCard(
				profile = nextProfile,
				modifier = Modifier
					.fillMaxSize()
					.padding(horizontal = dim.extraSmall / 2)
					.graphicsLayer {
						alpha = nextCardAlpha
					}
			)
		}
		ProfileCard(
			profile = currentProfile,
			onMoreClick = onMoreClick,
			modifier = Modifier
				.fillMaxSize()
				.padding(horizontal = dim.extraSmall / 2)
				.graphicsLayer {
					translationX = offsetX
					translationY = offsetY
					rotationZ = rotation
					scaleX = currentCardScale
					scaleY = currentCardScale
					alpha = currentCardAlpha
				}
				.pointerInput(currentProfile.id) {
					detectDragGestures(
						onDrag = { change, dragAmount ->
							change.consume()
							offsetX += dragAmount.x
							offsetY = (offsetY + dragAmount.y).coerceAtMost(0f)
							rotation = (offsetX / SwipeRotationDivisor)
								.coerceIn(-SwipeRotationLimit, SwipeRotationLimit)
						},
						onDragEnd = {
							val targetX = offsetX
							val targetY = offsetY
							val targetRotation = rotation
							val direction = when {
								abs(targetY) > SwipeDismissThreshold && abs(targetY) > abs(targetX) ->
									SwipeDirection.Up
								abs(targetX) > SwipeDismissThreshold ->
									if (targetX >= 0f) SwipeDirection.Right else SwipeDirection.Left
								else -> null
							}
							scope.launch {
								if (direction != null) {
									val dismissXTarget = if (direction == SwipeDirection.Up) {
										val verticalDistance = abs(targetY).coerceAtLeast(1f)
										targetX * (verticalDismissOffset / verticalDistance)
									} else {
										when (direction) {
											SwipeDirection.Right -> SwipeDismissOffset
											SwipeDirection.Left -> -SwipeDismissOffset
											SwipeDirection.Up -> 0f
										}
									}
									val dismissRotationTarget = if (direction == SwipeDirection.Up) {
										(targetX / abs(targetY).coerceAtLeast(1f) * SwipeRotationLimit)
											.coerceIn(-SwipeRotationLimit, SwipeRotationLimit)
									} else {
										when (direction) {
											SwipeDirection.Right -> SwipeRotationLimit
											SwipeDirection.Left -> -SwipeRotationLimit
											SwipeDirection.Up -> 0f
										}
									}
									val dismissSpec = tween<Float>(
										durationMillis = if (direction == SwipeDirection.Up) {
											UpSwipeDismissDurationMillis
										} else {
											SwipeDismissDurationMillis
										},
										easing = FastOutSlowInEasing
									)
									listOf(
										async {
											animate(
												initialValue = targetX,
												targetValue = dismissXTarget,
												animationSpec = dismissSpec
											) { value, _ -> offsetX = value }
										},
										async {
											animate(
												initialValue = targetY,
												targetValue = when (direction) {
													SwipeDirection.Up -> -verticalDismissOffset
													SwipeDirection.Left, SwipeDirection.Right -> 0f
												},
												animationSpec = dismissSpec
											) { value, _ -> offsetY = value }
										},
										async {
											animate(
												initialValue = targetRotation,
												targetValue = dismissRotationTarget,
												animationSpec = dismissSpec
											) { value, _ -> rotation = value }
										}
									).awaitAll()
									onSwiped(direction)
								} else {
									listOf(
										async {
											animate(
												initialValue = targetX,
												targetValue = 0f,
												animationSpec = resetSpec
											) { value, _ -> offsetX = value }
										},
										async {
											animate(
												initialValue = targetY,
												targetValue = 0f,
												animationSpec = resetSpec
											) { value, _ -> offsetY = value }
										},
										async {
											animate(
												initialValue = targetRotation,
												targetValue = 0f,
												animationSpec = resetSpec
											) { value, _ -> rotation = value }
										}
									).awaitAll()
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
	val outerShape = RoundedCornerShape(shapes.veryBigRoundedValue)
	val innerShape = RoundedCornerShape(shapes.veryBigRoundedValue - dim.cardOuterPadding)

	CardFrame(
		modifier = modifier,
		outerShape = outerShape,
		innerShape = innerShape
	) {
				Image(
					painter = painterResource(profile.imageRes),
					contentDescription = "Фото ${profile.name}",
					contentScale = ContentScale.Crop,
					modifier = Modifier.fillMaxSize()
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
				ScrimInfoBlock {
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
			BottomSheetHandle()
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

@Composable
private fun CardFrame(
	modifier: Modifier = Modifier,
	outerShape: RoundedCornerShape,
	innerShape: RoundedCornerShape,
	content: @Composable BoxScope.() -> Unit
) {
	val dim = LocalDimensions.current

	Surface(
		modifier = modifier,
		shape = outerShape,
		color = Color.White
	) {
		Box(
			modifier = Modifier
				.fillMaxSize()
				.padding(dim.cardOuterPadding)
				.clip(innerShape),
			content = content
		)
	}
}

@Composable
private fun BoxScope.ScrimInfoBlock(content: @Composable ColumnScope.() -> Unit) {
	val dim = LocalDimensions.current
	val extraColors = LocalTmatchExtraColors.current

	Column(
		modifier = Modifier
			.align(Alignment.BottomStart)
			.fillMaxWidth()
			.background(extraColors.baseScrim)
			.padding(dim.medium),
		verticalArrangement = Arrangement.spacedBy(dim.small - dim.extraSmall / 2),
		content = content
	)
}

@Composable
private fun ColumnScope.BottomSheetHandle() {
	val dim = LocalDimensions.current
	val colors = MaterialTheme.colorScheme

	Box(
		modifier = Modifier
			.align(Alignment.CenterHorizontally)
			.fillMaxWidth(0.18f)
			.height(dim.sheetHandleHeight)
			.clip(CircleShape)
			.background(colors.onSurface.copy(alpha = 0.28f))
	)
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainMatchingScreenMock(modifier: Modifier = Modifier) {
	val profiles = remember { MatchingContentController.profiles() }
	var currentIndex by rememberSaveable { mutableIntStateOf(0) }
	var isActionsSheetVisible by rememberSaveable { mutableStateOf(false) }
	val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
	val scope = rememberCoroutineScope()
	val currentProfile = profiles[currentIndex % profiles.size]
	val nextProfile =
		profiles.getOrNull((currentIndex + 1) % profiles.size)?.takeIf { it.id != currentProfile.id }

	val controller = object : MainMatchingContentController {
		override val currentProfile: MatchProfile = currentProfile
		override val nextProfile: MatchProfile? = nextProfile
		override val isActionsSheetVisible: Boolean = isActionsSheetVisible
		override val sheetState: SheetState = sheetState

		override fun onMoreClick() {
			isActionsSheetVisible = true
		}

		override fun onSwiped(direction: SwipeDirection) {
			currentIndex = (currentIndex + 1) % profiles.size
		}

		override fun onDismissSheet() {
			scope.launch {
				sheetState.hide()
				isActionsSheetVisible = false
			}
		}
	}

	MainMatchingContent(
		controller = controller,
		modifier = modifier
	)
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
