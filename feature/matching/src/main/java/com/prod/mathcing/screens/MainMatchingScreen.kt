package com.prod.matching.screens

import androidx.compose.animation.core.Animatable
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
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.prod.core.ui.theme.LocalDimensions
import com.prod.core.ui.theme.LocalShapes
import com.prod.matching.R
import com.prod.matching.model.MatchProfile
import com.prod.matching.model.MatchingContentController
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

private data class SwipeAffordanceSpec(
	val direction: SwipeDirection,
	val color: Color,
	val iconColor: Color,
	val iconRes: Int,
	val alignment: Alignment,
	val rotation: Float = 0f
)

private data class SwipeDeckVisualState(
	val verticalDismissOffset: Float,
	val activeDirection: SwipeDirection?,
	val swipeProgress: Float,
	val upSwipeProgress: Float,
	val nextCardAlpha: Float,
	val currentCardScale: Float,
	val currentCardAlpha: Float
)

private data class SwipeDismissTargets(
	val translationX: Float,
	val translationY: Float,
	val rotation: Float,
	val durationMillis: Int
)

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
private const val SwipeAffordanceFadeOutDurationMillis = 180

// Keeps all visual derivations of the current drag in one place so the deck composable
// can read like a layout description instead of a math-heavy gesture handler.
private fun buildSwipeDeckVisualState(
	offsetX: Float,
	offsetY: Float,
	deckHeight: Int
): SwipeDeckVisualState {
	val verticalDismissOffset = deckHeight.toFloat() + SwipeOffscreenMargin
	val swipeDistance = maxOf(abs(offsetX), abs(offsetY))
	val swipeProgress = (swipeDistance / SwipeDismissOffset).coerceIn(0f, 1f)
	val upSwipeProgress = (-offsetY / SwipeDismissOffset).coerceIn(0f, 1f)

	return SwipeDeckVisualState(
		verticalDismissOffset = verticalDismissOffset,
		activeDirection = resolveActiveSwipeDirection(offsetX = offsetX, offsetY = offsetY),
		swipeProgress = swipeProgress,
		upSwipeProgress = upSwipeProgress,
		nextCardAlpha = NextCardMinAlpha + (1f - NextCardMinAlpha) * swipeProgress,
		currentCardScale = 1f - UpSwipeScaleDelta * upSwipeProgress,
		currentCardAlpha = 1f - UpSwipeAlphaDelta * upSwipeProgress
	)
}

private fun resolveActiveSwipeDirection(
	offsetX: Float,
	offsetY: Float
): SwipeDirection? = when {
	offsetY < 0f && abs(offsetY) > abs(offsetX) -> SwipeDirection.Up
	offsetX > 0f -> SwipeDirection.Right
	offsetX < 0f -> SwipeDirection.Left
	else -> null
}

private fun resolveDismissDirection(
	offsetX: Float,
	offsetY: Float
): SwipeDirection? = when {
	abs(offsetY) > SwipeDismissThreshold && abs(offsetY) > abs(offsetX) -> SwipeDirection.Up
	abs(offsetX) > SwipeDismissThreshold ->
		if (offsetX >= 0f) SwipeDirection.Right else SwipeDirection.Left

	else -> null
}

private fun buildSwipeAffordances(colors: androidx.compose.material3.ColorScheme): List<SwipeAffordanceSpec> =
	listOf(
		SwipeAffordanceSpec(
			direction = SwipeDirection.Right,
			color = colors.primaryContainer,
			iconColor = colors.onPrimaryContainer,
			iconRes = R.drawable.ic_like_24,
			alignment = Alignment.TopStart,
			rotation = -10f
		),
		SwipeAffordanceSpec(
			direction = SwipeDirection.Left,
			color = colors.errorContainer,
			iconColor = colors.onErrorContainer,
			iconRes = R.drawable.ic_dislike_24,
			alignment = Alignment.TopEnd,
			rotation = 10f
		),
		SwipeAffordanceSpec(
			direction = SwipeDirection.Up,
			color = colors.tertiaryContainer,
			iconColor = colors.onTertiaryContainer,
			iconRes = R.drawable.ic_skip_24,
			alignment = Alignment.BottomCenter
		)
	)

private fun swipeAffordanceProgress(
	affordanceDirection: SwipeDirection,
	activeDirection: SwipeDirection?,
	swipeProgress: Float,
	upSwipeProgress: Float
): Float = when (affordanceDirection) {
	SwipeDirection.Up -> if (activeDirection == SwipeDirection.Up) upSwipeProgress else 0f
	SwipeDirection.Left, SwipeDirection.Right ->
		if (activeDirection == affordanceDirection) swipeProgress else 0f
}

private fun buildDismissTargets(
	direction: SwipeDirection,
	offsetX: Float,
	offsetY: Float,
	verticalDismissOffset: Float
): SwipeDismissTargets {
	val dismissXTarget = if (direction == SwipeDirection.Up) {
		val verticalDistance = abs(offsetY).coerceAtLeast(1f)
		offsetX * (verticalDismissOffset / verticalDistance)
	} else {
		when (direction) {
			SwipeDirection.Right -> SwipeDismissOffset
			SwipeDirection.Left -> -SwipeDismissOffset
			SwipeDirection.Up -> 0f
		}
	}
	val dismissYTarget = when (direction) {
		SwipeDirection.Up -> -verticalDismissOffset
		SwipeDirection.Left, SwipeDirection.Right -> 0f
	}
	val dismissRotationTarget = if (direction == SwipeDirection.Up) {
		(offsetX / abs(offsetY).coerceAtLeast(1f) * SwipeRotationLimit)
			.coerceIn(-SwipeRotationLimit, SwipeRotationLimit)
	} else {
		when (direction) {
			SwipeDirection.Right -> SwipeRotationLimit
			SwipeDirection.Left -> -SwipeRotationLimit
			SwipeDirection.Up -> 0f
		}
	}

	return SwipeDismissTargets(
		translationX = dismissXTarget,
		translationY = dismissYTarget,
		rotation = dismissRotationTarget,
		durationMillis = if (direction == SwipeDirection.Up) {
			UpSwipeDismissDurationMillis
		} else {
			SwipeDismissDurationMillis
		}
	)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainMatchingScreen(modifier: Modifier = Modifier) {
	MainMatchingScreenMock(modifier = modifier)
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
	var deckHeight by remember { mutableIntStateOf(0) }
	val scope = rememberCoroutineScope()
	val colors = MaterialTheme.colorScheme
	val resetSpec = remember {
		tween<Float>(
			durationMillis = SwipeDismissDurationMillis,
			easing = FastOutSlowInEasing
		)
	}
	val swipeAffordances = remember(colors.primary, colors.error, colors.tertiary) {
		buildSwipeAffordances(colors)
	}
	val affordanceByDirection = remember(swipeAffordances) {
		swipeAffordances.associateBy(SwipeAffordanceSpec::direction)
	}
	val affordanceOverlayModifier = remember(dim.extraSmall) {
		Modifier
			.fillMaxSize()
			.padding(horizontal = dim.extraSmall / 2)
	}
	var dismissedAffordance by remember { mutableStateOf<SwipeAffordanceSpec?>(null) }
	val dismissedAffordanceProgress = remember { Animatable(0f) }

	Box(
		modifier = modifier
			.fillMaxWidth()
			.onSizeChanged { deckHeight = it.height },
		contentAlignment = Alignment.Center
	) {
		val visualState = buildSwipeDeckVisualState(
			offsetX = offsetX,
			offsetY = offsetY,
			deckHeight = deckHeight
		)

		if (nextProfile != null) {
			ProfileCard(
				profile = nextProfile,
				modifier = Modifier
					.fillMaxSize()
					.padding(horizontal = dim.extraSmall / 2)
					.graphicsLayer {
						alpha = visualState.nextCardAlpha
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
					scaleX = visualState.currentCardScale
					scaleY = visualState.currentCardScale
					alpha = visualState.currentCardAlpha
				}
				.pointerInput(currentProfile.id) {
					detectDragGestures(
						onDrag = { change, dragAmount ->
							change.consume()

							// During drag we update raw values directly so affordances stay perfectly
							// locked to the finger, without any extra animation state in-between.
							offsetX += dragAmount.x
							offsetY = (offsetY + dragAmount.y).coerceAtMost(0f)
							rotation = (offsetX / SwipeRotationDivisor)
								.coerceIn(-SwipeRotationLimit, SwipeRotationLimit)
						},
						onDragEnd = {
							val targetX = offsetX
							val targetY = offsetY
							val targetRotation = rotation
							val direction = resolveDismissDirection(
								offsetX = targetX,
								offsetY = targetY
							)

							scope.launch {
								if (direction != null) {
									// Once the threshold is crossed we animate the card to the same
									// off-screen targets that the final action implies.
									val dismissTargets = buildDismissTargets(
										direction = direction,
										offsetX = targetX,
										offsetY = targetY,
										verticalDismissOffset = visualState.verticalDismissOffset
									)
									val dismissSpec = tween<Float>(
										durationMillis = dismissTargets.durationMillis,
										easing = FastOutSlowInEasing
									)
									listOf(
										async {
											animate(
												initialValue = targetX,
												targetValue = dismissTargets.translationX,
												animationSpec = dismissSpec
											) { value, _ -> offsetX = value }
										},
										async {
											animate(
												initialValue = targetY,
												targetValue = dismissTargets.translationY,
												animationSpec = dismissSpec
											) { value, _ -> offsetY = value }
										},
										async {
											animate(
												initialValue = targetRotation,
												targetValue = dismissTargets.rotation,
												animationSpec = dismissSpec
											) { value, _ -> rotation = value }
										}
									).awaitAll()

									// After the card is accepted/rejected/skipped, keep the same
									// affordance visible for a short fade so the result does not
									// disappear on the exact frame when the next profile appears.
									dismissedAffordance = affordanceByDirection[direction]
									dismissedAffordanceProgress.snapTo(1f)
									onSwiped(direction)
									dismissedAffordanceProgress.animateTo(
										targetValue = 0f,
										animationSpec = tween(
											durationMillis = SwipeAffordanceFadeOutDurationMillis,
											easing = FastOutSlowInEasing
										)
									)
									dismissedAffordance = null
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
		swipeAffordances.forEach { affordance ->
			val rawProgress = swipeAffordanceProgress(
				affordanceDirection = affordance.direction,
				activeDirection = visualState.activeDirection,
				swipeProgress = visualState.swipeProgress,
				upSwipeProgress = visualState.upSwipeProgress
			)

			if (rawProgress > 0.001f) {
				SwipeActionAffordance(
					affordance = affordance,
					progress = rawProgress,
					modifier = affordanceOverlayModifier
				)
			}
		}
		if (visualState.activeDirection == null && dismissedAffordance != null) {
			SwipeActionAffordance(
				affordance = dismissedAffordance!!,
				progress = dismissedAffordanceProgress.value,
				modifier = affordanceOverlayModifier
			)
		}
	}
}

@Composable
private fun SwipeActionAffordance(
	affordance: SwipeAffordanceSpec,
	progress: Float,
	modifier: Modifier = Modifier
) {
	val dim = LocalDimensions.current

	Box(
		modifier = modifier
			.clip(RoundedCornerShape(28.dp))
			// The tint follows the card position directly, so the feedback feels attached
			// to the gesture instead of behaving like a separate animation layer.
			.background(affordance.color.copy(alpha = 0.24f * progress))
	) {
		Surface(
			modifier = Modifier
				.align(affordance.alignment)
				.offset {
					when (affordance.direction) {
						SwipeDirection.Left -> IntOffset(-24, 20)
						SwipeDirection.Right -> IntOffset(24, 20)
						SwipeDirection.Up -> IntOffset(0, -20)
					}
				}
				.graphicsLayer {
					alpha = (progress * 1.8f).coerceIn(0f, 1f)
					scaleX = 0.88f + 0.12f * progress
					scaleY = 0.88f + 0.12f * progress
					rotationZ = affordance.rotation * progress
				},
			shape = LocalShapes.current.mediumRounded,
			color = affordance.color.copy(alpha = 0.92f),
			contentColor = Color.White
		) {
			Row(
				modifier = Modifier.padding(
					PaddingValues(
						horizontal = dim.small + dim.extraSmall,
						vertical = dim.verySmall + dim.extraSmall / 2
					)
				),
				horizontalArrangement = Arrangement.spacedBy(dim.verySmall),
				verticalAlignment = Alignment.CenterVertically
			) {
				Icon(
					painter = painterResource(affordance.iconRes),
					contentDescription = null,
					modifier = Modifier.size(dim.iconMedium),
					tint = affordance.iconColor
				)
			}
		}
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
		profiles.getOrNull((currentIndex + 1) % profiles.size)
			?.takeIf { it.id != currentProfile.id }
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
