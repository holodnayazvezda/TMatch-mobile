package com.prod.matching.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.prod.common.api.model.RecommendationReasonSummary
import com.prod.common.api.model.UserProfileFeedAction
import com.prod.ui.theme.LocalDimensions
import com.prod.core.ui.theme.LocalShapes
import com.prod.matching.R
import com.prod.matching.domain.model.MatchAction
import com.prod.matching.model.MatchingDeckItem
import com.prod.matching.model.MatchingDeckItem.Profile
import com.prod.matching.presentation.MainMatchingViewModel
import com.prod.ui.R as UiR
import com.prod.ui.components.AppBottomScrimInfo
import com.prod.ui.components.AppBottomSheetHandle
import com.prod.ui.components.AppHeroCardFrame
import com.prod.ui.components.AppInfoChip
import com.prod.ui.components.AppOverlayIconBadge
import com.prod.ui.components.AppScreenTopBar
import com.prod.ui.components.AppTopBarIconAction
import com.prod.ui.theme.LocalTmatchExtraColors
import com.prod.ui.theme.TmatchTheme
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
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
	deckHeight: Int,
	allowedDirections: Set<SwipeDirection>
): SwipeDeckVisualState {
	val verticalDismissOffset = deckHeight.toFloat() + SwipeOffscreenMargin
	val swipeDistance = maxOf(abs(offsetX), abs(offsetY))
	val swipeProgress = (swipeDistance / SwipeDismissOffset).coerceIn(0f, 1f)
	val upSwipeProgress = (-offsetY / SwipeDismissOffset).coerceIn(0f, 1f)

		return SwipeDeckVisualState(
			verticalDismissOffset = verticalDismissOffset,
			activeDirection = resolveActiveSwipeDirection(
				offsetX = offsetX,
				offsetY = offsetY,
				allowedDirections = allowedDirections
			),
			swipeProgress = swipeProgress,
			upSwipeProgress = upSwipeProgress,
			nextCardAlpha = NextCardMinAlpha + (1f - NextCardMinAlpha) * swipeProgress,
		currentCardScale = 1f - UpSwipeScaleDelta * upSwipeProgress,
		currentCardAlpha = 1f - UpSwipeAlphaDelta * upSwipeProgress
	)
}

private fun resolveActiveSwipeDirection(
	offsetX: Float,
	offsetY: Float,
	allowedDirections: Set<SwipeDirection>
): SwipeDirection? = when {
	SwipeDirection.Up in allowedDirections &&
		offsetY < 0f &&
		abs(offsetY) > abs(offsetX) -> SwipeDirection.Up
	SwipeDirection.Right in allowedDirections && offsetX > 0f -> SwipeDirection.Right
	SwipeDirection.Left in allowedDirections && offsetX < 0f -> SwipeDirection.Left
	else -> null
}

private fun resolveDismissDirection(
	offsetX: Float,
	offsetY: Float,
	allowedDirections: Set<SwipeDirection>
): SwipeDirection? = when {
	SwipeDirection.Up in allowedDirections &&
		abs(offsetY) > SwipeDismissThreshold &&
		abs(offsetY) > abs(offsetX) -> SwipeDirection.Up
	(SwipeDirection.Left in allowedDirections || SwipeDirection.Right in allowedDirections) &&
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
			iconRes = UiR.drawable.ic_like_24,
			alignment = Alignment.TopStart,
			rotation = -10f
		),
		SwipeAffordanceSpec(
			direction = SwipeDirection.Left,
			color = colors.errorContainer,
			iconColor = colors.onErrorContainer,
			iconRes = UiR.drawable.ic_dislike_24,
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
fun MainMatchingScreen(
	modifier: Modifier = Modifier,
	onUnauthorized: () -> Unit,
	onRequestNoInternet: (retryAction: () -> Unit) -> Unit,
	onOpenProfile: (profile: Profile) -> Unit,
	onProfileActionHandlerReady: ((userId: String, action: UserProfileFeedAction) -> Unit) -> Unit,
	viewModel: MainMatchingViewModel = koinViewModel()
) {
	LaunchedEffect(viewModel, onUnauthorized, onRequestNoInternet) {
		viewModel.initialize(
			onUnauthorized = onUnauthorized,
			onNoInternet = onRequestNoInternet
		)
	}
	LaunchedEffect(viewModel, onProfileActionHandlerReady) {
		onProfileActionHandlerReady { userId, action ->
			viewModel.submitProfileAction(
				userId = userId,
				action = when (action) {
					UserProfileFeedAction.Like -> MatchAction.Like
					UserProfileFeedAction.Dislike -> MatchAction.Dislike
				}
			)
		}
	}

	val uiState = viewModel.uiState
	val controller = object : MainMatchingContentController {
		override val currentItem: MatchingDeckItem? = uiState.currentItem
		override val nextItem: MatchingDeckItem? = uiState.nextItem
		override val isActionsSheetVisible: Boolean = uiState.isActionsSheetVisible

		override fun onMoreClick() {
			viewModel.onMoreClick()
		}

		override fun onSwiped(direction: SwipeDirection) {
			if (uiState.currentItem is MatchingDeckItem.Transition) {
				viewModel.onSwipeUp()
				return
			}

			when (direction) {
				SwipeDirection.Left -> viewModel.onSwipeLeft()
				SwipeDirection.Right -> viewModel.onSwipeRight()
				SwipeDirection.Up -> viewModel.onSwipeUp()
			}
		}

		override fun onDismissSheet() {
			viewModel.onDismissSheet()
		}

		override fun onOpenCurrentProfile() {
			viewModel.onDismissSheet()
			uiState.currentProfile?.let(onOpenProfile)
		}
	}

	MainMatchingContent(
		controller = controller,
		isLoading = uiState.isLoading,
		isLoadingMore = uiState.isLoadingMore,
		error = uiState.error,
		modifier = modifier
	)
}

@OptIn(ExperimentalMaterial3Api::class)
private interface MainMatchingContentController {
	val currentItem: MatchingDeckItem?
	val nextItem: MatchingDeckItem?
	val isActionsSheetVisible: Boolean

	fun onMoreClick()
	fun onSwiped(direction: SwipeDirection)
	fun onDismissSheet()
	fun onOpenCurrentProfile()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainMatchingContent(
	controller: MainMatchingContentController,
	isLoading: Boolean,
	isLoadingMore: Boolean,
	error: String?,
	modifier: Modifier = Modifier
) {
	val dim = LocalDimensions.current
	val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
	val scope = rememberCoroutineScope()
	var visibleError by remember { mutableStateOf<String?>(null) }

	LaunchedEffect(error) {
		if (error.isNullOrBlank()) {
			visibleError = null
			return@LaunchedEffect
		}

		visibleError = error
		delay(2800)
		if (visibleError == error) {
			visibleError = null
		}
	}

	Box(
		modifier = modifier.fillMaxSize()
	) {
		if (isLoading) {
			Text(
				text = stringResource(R.string.matching_loading),
				modifier = Modifier.align(Alignment.Center),
				style = MaterialTheme.typography.titleMedium,
				color = MaterialTheme.colorScheme.onBackground
			)
			return@Box
		}

		Column(
			modifier = Modifier
				.fillMaxSize()
				.padding(
					start = dim.cardStackInset,
					end = dim.cardStackInset,
					bottom = dim.defaultSpacing
				)
		) {
			MatchingHeader()
			Spacer(modifier = Modifier.height(dim.medium - dim.extraSmall))
			SwipeCardDeck(
				currentItem = controller.currentItem,
				nextItem = controller.nextItem,
				isLoadingMore = isLoadingMore,
				onMoreClick = controller::onMoreClick,
				onProfileClick = controller::onOpenCurrentProfile,
				onSwiped = controller::onSwiped,
				modifier = Modifier.weight(1f)
			)
		}
		AnimatedVisibility(
			visible = visibleError != null,
			modifier = Modifier
				.align(Alignment.TopCenter)
				.fillMaxWidth()
				.padding(
					start = dim.large,
					end = dim.large,
					top = dim.large + dim.defaultSpacing
				),
			enter = fadeIn(
				animationSpec = tween(durationMillis = 180)
			) + slideInVertically(
				initialOffsetY = { -it / 2 },
				animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing)
			),
			exit = fadeOut(
				animationSpec = tween(durationMillis = 180)
			) + slideOutVertically(
				targetOffsetY = { -it / 3 },
				animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing)
			)
		) {
			Surface(
				color = MaterialTheme.colorScheme.errorContainer,
				contentColor = MaterialTheme.colorScheme.onErrorContainer,
				shape = LocalShapes.current.mediumRounded,
				tonalElevation = 6.dp
			) {
				Text(
					text = visibleError.orEmpty(),
					modifier = Modifier.padding(dim.small),
					style = MaterialTheme.typography.bodyMedium
				)
			}
		}
		if (controller.isActionsSheetVisible) {
			MatchingActionsBottomSheet(
				onDismiss = {
					scope.launch {
						sheetState.hide()
						controller.onDismissSheet()
					}
				},
				onOpenProfile = controller::onOpenCurrentProfile,
				sheetState = sheetState
			)
		}
	}
}

@Composable
private fun MatchingHeader() {
	AppScreenTopBar(
		title = stringResource(R.string.matching_title),
		modifier = Modifier.fillMaxWidth(),
		actions = {
			AppTopBarIconAction(
				iconRes = R.drawable.ic_history_24,
				contentDescription = stringResource(R.string.matching_history),
				onClick = {}
			)
		}
	)
}

@Composable
private fun SwipeCardDeck(
	currentItem: MatchingDeckItem?,
	nextItem: MatchingDeckItem?,
	isLoadingMore: Boolean,
	onMoreClick: () -> Unit,
	onProfileClick: () -> Unit,
	onSwiped: (SwipeDirection) -> Unit,
	modifier: Modifier = Modifier
) {
	val dim = LocalDimensions.current
	val shapes = LocalShapes.current
	val currentProfile = currentItem as? Profile
	val nextProfile = nextItem as? Profile
	var offsetX by remember(currentItem) { mutableFloatStateOf(0f) }
	var offsetY by remember(currentItem) { mutableFloatStateOf(0f) }
	var rotation by remember(currentItem) { mutableFloatStateOf(0f) }
	var deckHeight by remember { mutableIntStateOf(0) }
	val scope = rememberCoroutineScope()
	val colors = MaterialTheme.colorScheme
	val allowedSwipeDirections = remember(currentItem) {
		SwipeDirection.entries.toSet()
	}
	val showSwipeAffordances = currentItem !is MatchingDeckItem.Transition
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
	val cardModifier = remember(dim.profileCardAspectRatio) {
		Modifier
			.fillMaxWidth()
			.aspectRatio(dim.profileCardAspectRatio)
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
				deckHeight = deckHeight,
				allowedDirections = allowedSwipeDirections
			)

			when (nextItem) {
				is Profile -> {
				ProfileCard(
					profile = nextItem,
				modifier = cardModifier
					.padding(horizontal = dim.extraSmall / 2)
					.graphicsLayer {
						alpha = visualState.nextCardAlpha
					}
			)
			}

				is MatchingDeckItem.Transition -> {
					RecommendationsTransitionCard(
						modifier = cardModifier
							.padding(horizontal = dim.extraSmall / 2)
							.graphicsLayer {
								alpha = visualState.nextCardAlpha
							}
					)
				}

				null -> {
				MatchingDeckPlaceholderCard(
				message = stringResource(R.string.matching_empty_message),
				modifier = cardModifier
					.padding(horizontal = dim.extraSmall / 2)
					.graphicsLayer {
						alpha = if (currentProfile != null) visualState.nextCardAlpha else 1f
					}
			)
			}
		}
		when (val activeItem = currentItem) {
			is Profile -> {
				ProfileCard(
					profile = activeItem,
					onMoreClick = onMoreClick,
					onClick = onProfileClick,
					modifier = cardModifier
						.padding(horizontal = dim.extraSmall / 2)
						.graphicsLayer {
							translationX = offsetX
							translationY = offsetY
							rotationZ = rotation
							scaleX = visualState.currentCardScale
							scaleY = visualState.currentCardScale
							alpha = visualState.currentCardAlpha
						}
						.pointerInput(activeItem.id) {
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
									val direction = resolveDismissDirection(
										offsetX = targetX,
										offsetY = targetY,
										allowedDirections = allowedSwipeDirections
									)

								scope.launch {
									if (direction != null) {
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
			}

			is MatchingDeckItem.Transition -> {
				RecommendationsTransitionCard(
					modifier = cardModifier
						.padding(horizontal = dim.extraSmall / 2)
						.graphicsLayer {
							translationX = offsetX
							translationY = offsetY
							rotationZ = rotation
							scaleX = visualState.currentCardScale
							scaleY = visualState.currentCardScale
							alpha = visualState.currentCardAlpha
						}
							.pointerInput(activeItem.id) {
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
											val direction = resolveDismissDirection(
												offsetX = targetX,
												offsetY = targetY,
												allowedDirections = allowedSwipeDirections
											)
										scope.launch {
											if (direction != null) {
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

			null -> Unit
		}
			if (showSwipeAffordances) {
				swipeAffordances
					.filter { it.direction in allowedSwipeDirections }
					.forEach { affordance ->
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
								modifier = cardModifier
									.clip(shapes.largeRounded)
									.then(affordanceOverlayModifier)
							)
						}
					}
				if (
					visualState.activeDirection == null &&
					dismissedAffordance != null &&
					dismissedAffordance?.direction in allowedSwipeDirections
				) {
					val affordance = dismissedAffordance ?: return@Box
					SwipeActionAffordance(
						affordance = affordance,
						progress = dismissedAffordanceProgress.value,
						modifier = cardModifier
							.clip(shapes.largeRounded)
							.then(affordanceOverlayModifier)
					)
				}
			}
		}
	}

@Composable
private fun MatchingDeckPlaceholderCard(
	message: String,
	modifier: Modifier = Modifier
) {
	val dim = LocalDimensions.current

	AppHeroCardFrame(modifier = modifier) {
		val contentColor = MaterialTheme.colorScheme.onBackground
		Box(
			modifier = Modifier
				.fillMaxSize()
				.background(MaterialTheme.colorScheme.surfaceContainerHigh),
			contentAlignment = Alignment.Center
		) {
			Column(
				modifier = Modifier.padding(dim.large),
				verticalArrangement = Arrangement.spacedBy(dim.small),
				horizontalAlignment = Alignment.CenterHorizontally
			) {
				Icon(
					painter = painterResource(R.drawable.ic_history_24),
					contentDescription = null,
					modifier = Modifier.size(dim.extraLarge),
					tint = MaterialTheme.colorScheme.primary
				)
				Text(
					text = message,
					textAlign = TextAlign.Center,
					style = MaterialTheme.typography.titleMedium,
					color = MaterialTheme.colorScheme.onSurface
				)
			}
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
			.clip(LocalShapes.current.largeRounded)
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
	profile: Profile,
	modifier: Modifier = Modifier,
	onClick: (() -> Unit)? = null,
	onMoreClick: (() -> Unit)? = null
) {
	val dim = LocalDimensions.current
	val typo = MaterialTheme.typography
	val extraColors = LocalTmatchExtraColors.current
	val displayTitle = profile.age?.let {
		stringResource(R.string.matching_profile_title_with_age, profile.name, it)
	} ?: profile.name
	val incomingLikeTransition = rememberInfiniteTransition(label = "incomingLikeCard")
	val incomingLikeGlow = incomingLikeTransition.animateFloat(
		initialValue = 0f,
		targetValue = 1f,
		animationSpec = infiniteRepeatable(
			animation = tween(
				durationMillis = 1400,
				easing = FastOutSlowInEasing
			),
			repeatMode = RepeatMode.Reverse
		),
		label = "incomingLikeGlow"
	)
	val animatedFrameColor = if (profile.isIncomingLike) {
		lerp(
			start = Color.White,
			stop = extraColors.accentGold.copy(alpha = 0.82f),
			fraction = incomingLikeGlow.value * 0.55f
		)
	} else {
		Color.White
	}

	AppHeroCardFrame(
		modifier = if (onClick != null) {
			modifier.clickable(onClick = onClick)
		} else {
			modifier
		},
		containerColor = animatedFrameColor
	) {
		AsyncImage(
			model = profile.imageUrl,
			contentDescription = stringResource(R.string.matching_photo_description, profile.name),
			contentScale = ContentScale.Crop,
			placeholder = painterResource(R.drawable.match_user_placeholder),
			error = painterResource(R.drawable.match_user_placeholder),
			fallback = painterResource(R.drawable.match_user_placeholder),
			modifier = Modifier.fillMaxSize()
		)
		if (onMoreClick != null) {
			AppOverlayIconBadge(
				iconRes = UiR.drawable.ic_menu_24,
				modifier = Modifier
					.padding(dim.medium - dim.extraSmall)
					.size(dim.iconContainer + dim.verySmall - dim.extraSmall)
					.align(Alignment.TopEnd)
					.clip(CircleShape)
					.clickable(onClick = onMoreClick)
			)
		}
		AppBottomScrimInfo {
			Text(
				text = displayTitle,
				style = typo.titleLarge,
				color = extraColors.onBaseScrim
			)
			FlowRow(
				modifier = Modifier.wrapContentWidth(),
				horizontalArrangement = Arrangement.spacedBy(dim.extraSmall),
				verticalArrangement = Arrangement.spacedBy(dim.extraSmall)
			) {
				profile.reasons.forEach { reason ->
					AppInfoChip(
						text = reason.label,
						containerColor = extraColors.accentGold,
						contentColor = extraColors.onBaseScrim
					)
				}
			}
		}
	}
}

@Composable
private fun RecommendationsTransitionCard(
	modifier: Modifier = Modifier
) {
	val dim = LocalDimensions.current
	val contentColor = MaterialTheme.colorScheme.onBackground
	val arrowTransition = rememberInfiniteTransition(label = "transitionArrow")
	val arrowOffsetY = arrowTransition.animateFloat(
		initialValue = 0f,
		targetValue = -18f,
		animationSpec = infiniteRepeatable(
			animation = tween(
				durationMillis = 900,
				easing = FastOutSlowInEasing
			),
			repeatMode = RepeatMode.Reverse
		),
		label = "transitionArrowOffset"
	)
	val arrowAlpha = arrowTransition.animateFloat(
		initialValue = 0.55f,
		targetValue = 1f,
		animationSpec = infiniteRepeatable(
			animation = tween(
				durationMillis = 900,
				easing = FastOutSlowInEasing
			),
			repeatMode = RepeatMode.Reverse
		),
		label = "transitionArrowAlpha"
	)

		AppHeroCardFrame(modifier = modifier) {
			Box(
				modifier = Modifier
					.fillMaxSize()
					.background(MaterialTheme.colorScheme.background),
				contentAlignment = Alignment.Center
			) {
			Column(
				modifier = Modifier.padding(dim.large),
				verticalArrangement = Arrangement.spacedBy(dim.small),
				horizontalAlignment = Alignment.CenterHorizontally
			) {
				Icon(
					painter = painterResource(UiR.drawable.outline_arrow_drop_down_24),
					contentDescription = null,
					modifier = Modifier
						.size(dim.extraLarge)
						.graphicsLayer {
							translationY = arrowOffsetY.value
							alpha = arrowAlpha.value
							rotationZ = 180f
						},
					tint = contentColor
				)
				Text(
					text = stringResource(R.string.matching_transition_title),
					textAlign = TextAlign.Center,
					style = MaterialTheme.typography.headlineSmall,
					color = contentColor
				)
				Text(
					text = stringResource(R.string.matching_transition_message),
					textAlign = TextAlign.Center,
					style = MaterialTheme.typography.bodyLarge,
					color = contentColor
				)
				Text(
					text = stringResource(R.string.matching_transition_cta),
					textAlign = TextAlign.Center,
					style = MaterialTheme.typography.titleSmall,
					color = contentColor
				)
			}
		}
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MatchingActionsBottomSheet(
	onDismiss: () -> Unit,
	onOpenProfile: () -> Unit,
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
			AppBottomSheetHandle(
				modifier = Modifier.align(Alignment.CenterHorizontally)
			)
			ActionSheetButton(
				text = stringResource(R.string.matching_open_profile),
				onClick = onOpenProfile,
				accent = colors.primaryContainer,
				onAccent = colors.onPrimaryContainer
			)
			ActionSheetButton(
				text = stringResource(R.string.matching_block),
				onClick = onDismiss,
				accent = colors.errorContainer,
				onAccent = colors.onErrorContainer
			)
			ActionSheetButton(
				text = stringResource(R.string.matching_close),
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

@Preview(showBackground = true, backgroundColor = 0xFF07090E, widthDp = 390, heightDp = 844)
@Composable
private fun MainMatchingScreenPreview() {
	val previewProfiles = remember {
		listOf(
			Profile(
				id = "preview-current",
				name = "Марина Орлова",
				age = 63,
				reasons = listOf(
					RecommendationReasonSummary("Общая активность", "Похожий ритм ежедневной активности."),
					RecommendationReasonSummary("Рестораны", "Похожие предпочтения по тратам на кафе и рестораны."),
					RecommendationReasonSummary("Вечерняя активность", "Чаще активны в одно и то же время суток.")
				),
				imageUrl = null,
				recommendationId = "preview-rec-1"
			),
			Profile(
				id = "preview-next",
				name = "Александр Белый",
				age = 70,
				reasons = listOf(
					RecommendationReasonSummary("Транспорт", "Схожий паттерн пользования транспортом."),
					RecommendationReasonSummary("Любимый продавец", "Есть пересечения по устойчивым предпочтениям.")
				),
				imageUrl = null,
				recommendationId = "preview-rec-2"
			)
		)
	}
	var currentIndex by rememberSaveable { mutableIntStateOf(0) }
	var isActionsSheetVisible by rememberSaveable { mutableStateOf(false) }
	val previewController = object : MainMatchingContentController {
		override val currentItem: MatchingDeckItem?
			get() = previewProfiles.getOrNull(currentIndex)
		override val nextItem: MatchingDeckItem?
			get() = previewProfiles.getOrNull(currentIndex + 1)
		override val isActionsSheetVisible: Boolean
			get() = isActionsSheetVisible

		override fun onMoreClick() {
			isActionsSheetVisible = true
		}

		override fun onSwiped(direction: SwipeDirection) {
			currentIndex += 1
			isActionsSheetVisible = false
		}

		override fun onDismissSheet() {
			isActionsSheetVisible = false
		}

		override fun onOpenCurrentProfile() {
			isActionsSheetVisible = false
		}
	}

	TmatchTheme(darkTheme = true) {
		Scaffold {
			MainMatchingContent(
				controller = previewController,
				isLoading = false,
				isLoadingMore = false,
				error = null,
				modifier = Modifier.padding(it)
			)
		}
	}
}
