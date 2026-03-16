package com.prod.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.prod.ui.theme.LocalDimensions
import com.prod.core.ui.theme.LocalShapes
import com.prod.ui.theme.LocalTmatchExtraColors

@Composable
fun AppScrollableScreen(
	modifier: Modifier = Modifier,
	contentPaddingTop: Boolean = true,
	content: @Composable ColumnScope.() -> Unit
) {
	val dim = LocalDimensions.current

	Column(
		modifier = modifier
			.fillMaxSize()
			.safeDrawingPadding()
			.verticalScroll(rememberScrollState())
			.padding(
				horizontal = dim.large,
				vertical = if (contentPaddingTop) dim.defaultSpacing else dim.screenVerticalPadding
			),
		verticalArrangement = Arrangement.spacedBy(dim.defaultSpacing),
		content = content
	)
}

@Composable
fun AppCenteredContentColumn(
	modifier: Modifier = Modifier,
	content: @Composable ColumnScope.() -> Unit
) {
	val dim = LocalDimensions.current

	Column(
		modifier = modifier
			.fillMaxWidth()
			.widthIn(max = 360.dp)
			.padding(horizontal = dim.screenHorizontalPadding),
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.spacedBy(dim.defaultSpacing),
		content = content
	)
}

@Composable
fun AppScreenTopBar(
	title: String,
	modifier: Modifier = Modifier,
	horizontalPadding: Dp = LocalDimensions.current.small,
	verticalPadding: Dp = LocalDimensions.current.verySmall,
	applySafeDrawingPadding: Boolean = true,
	navigation: (@Composable RowScope.() -> Unit)? = null,
	actions: (@Composable RowScope.() -> Unit)? = null
) {
	Row(
		modifier = modifier
			.fillMaxWidth()
			.then(
				if (applySafeDrawingPadding) {
					Modifier.safeDrawingPadding()
				} else {
					Modifier
				}
			)
			.padding(horizontal = horizontalPadding, vertical = verticalPadding),
		verticalAlignment = Alignment.CenterVertically
	) {
		Row(
			modifier = Modifier.weight(1f),
			verticalAlignment = Alignment.CenterVertically,
			content = {
				navigation?.invoke(this)
			}
		)
		Text(
			text = title,
			modifier = Modifier.weight(1f),
			style = MaterialTheme.typography.titleLarge,
			color = MaterialTheme.colorScheme.onBackground,
			textAlign = TextAlign.Center
		)
		Row(
			modifier = Modifier.weight(1f),
			horizontalArrangement = Arrangement.End,
			verticalAlignment = Alignment.CenterVertically,
			content = {
				actions?.invoke(this)
			}
		)
	}
}

@Composable
fun AppSectionCard(
	modifier: Modifier = Modifier,
	containerColor: Color = MaterialTheme.colorScheme.surface,
	contentColor: Color = MaterialTheme.colorScheme.onSurface,
	shape: Shape = LocalShapes.current.sectionRounded,
	tonalElevation: Dp = 2.dp,
	content: @Composable ColumnScope.() -> Unit
) {
	val dim = LocalDimensions.current

	Surface(
		modifier = modifier,
		shape = shape,
		color = containerColor,
		contentColor = contentColor,
		tonalElevation = tonalElevation
	) {
		Column(
			modifier = Modifier.padding(
				horizontal = dim.sectionHorizontalPadding,
				vertical = dim.sectionVerticalPadding
			),
			verticalArrangement = Arrangement.spacedBy(dim.small),
			content = content
		)
	}
}

@Composable
fun AppSectionTitle(
	title: String,
	modifier: Modifier = Modifier
) {
	Text(
		text = title,
		modifier = modifier,
		style = MaterialTheme.typography.titleMedium,
		color = MaterialTheme.colorScheme.onSurface,
		fontWeight = FontWeight.SemiBold
	)
}

@Composable
fun AppInfoChip(
	text: String,
	modifier: Modifier = Modifier,
	containerColor: Color = MaterialTheme.colorScheme.secondaryContainer,
	contentColor: Color = MaterialTheme.colorScheme.onSecondaryContainer
) {
	val dim = LocalDimensions.current
	val shapes = LocalShapes.current

	Surface(
		modifier = modifier,
		shape = shapes.pillRounded,
		color = containerColor,
		contentColor = contentColor
	) {
		Text(
			text = text,
			modifier = Modifier.padding(
				horizontal = dim.pillHorizontalPadding,
				vertical = dim.pillVerticalPadding
			),
			style = MaterialTheme.typography.labelLarge
		)
	}
}

@Composable
fun AppFactCard(
	title: String,
	value: String,
	modifier: Modifier = Modifier
) {
	AppSectionCard(
		modifier = modifier,
		containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
		shape = LocalShapes.current.mediumRounded,
		tonalElevation = 0.dp
	) {
		Text(
			text = title,
			style = MaterialTheme.typography.labelMedium,
			color = MaterialTheme.colorScheme.onSurfaceVariant
		)
		Text(
			text = value,
			style = MaterialTheme.typography.titleMedium,
			color = MaterialTheme.colorScheme.onSurface,
			fontWeight = FontWeight.SemiBold
		)
	}
}

@Composable
fun AppCircularActionButton(
	iconRes: Int,
	contentDescription: String,
	onClick: () -> Unit,
	modifier: Modifier = Modifier,
	containerColor: Color,
	contentColor: Color
) {
	val dim = LocalDimensions.current

	Surface(
		modifier = modifier,
		shape = CircleShape,
		color = containerColor,
		contentColor = contentColor
	) {
		IconButton(
			onClick = onClick,
			modifier = Modifier.size(dim.largeIconContainer)
		) {
			Icon(
				painter = painterResource(iconRes),
				contentDescription = contentDescription,
				tint = contentColor
			)
		}
	}
}

@Composable
fun AppOverlayIconBadge(
	iconRes: Int,
	modifier: Modifier = Modifier,
	tint: Color = LocalTmatchExtraColors.current.onBaseScrim
) {
	val dim = LocalDimensions.current
	val extraColors = LocalTmatchExtraColors.current

	Surface(
		modifier = modifier,
		shape = CircleShape,
		color = extraColors.baseScrim,
		contentColor = tint
	) {
		Box(contentAlignment = Alignment.Center) {
			Icon(
				painter = painterResource(iconRes),
				contentDescription = null,
				modifier = Modifier.size(dim.iconSmall),
				tint = tint
			)
		}
	}
}

@Composable
fun AppHeroCardFrame(
	modifier: Modifier = Modifier,
	containerColor: Color = Color.White,
	content: @Composable BoxScope.() -> Unit
) {
	val dim = LocalDimensions.current
	val shapes = LocalShapes.current
	val innerShape = RoundedCornerShape(shapes.veryBigRoundedValue - dim.cardOuterPadding)

	Surface(
		modifier = modifier,
		shape = shapes.largeRounded,
		color = containerColor
	) {
		Box(
			modifier = Modifier
				.fillMaxSize()
				.padding(dim.cardOuterPadding),
			content = {
				Box(
					modifier = Modifier
						.fillMaxSize()
						.clip(innerShape),
					content = content
				)
			}
		)
	}
}

@Composable
fun AppBottomSheetHandle(
	modifier: Modifier = Modifier
) {
	val dim = LocalDimensions.current
	val colors = MaterialTheme.colorScheme

	Box(
		modifier = modifier
			.fillMaxWidth(0.18f)
			.height(dim.sheetHandleHeight)
			.background(colors.onSurface.copy(alpha = 0.28f), CircleShape)
	)
}

@Composable
fun BoxScope.AppHeroPagerIndicator(
	total: Int,
	current: Int,
	modifier: Modifier = Modifier,
	activeColor: Color = MaterialTheme.colorScheme.surface,
	inactiveColor: Color = MaterialTheme.colorScheme.surface.copy(alpha = 0.45f)
) {
	if (total <= 1) return

	val dim = LocalDimensions.current

	Row(
		modifier = modifier
			.align(Alignment.TopCenter)
			.padding(top = dim.pagerIndicatorInset),
		horizontalArrangement = Arrangement.spacedBy(dim.pagerIndicatorGap),
		verticalAlignment = Alignment.CenterVertically
	) {
		repeat(total) { index ->
			Surface(
				modifier = Modifier.size(
					width = if (current == index) dim.pagerIndicatorActiveWidth else dim.pagerIndicatorInactiveWidth,
					height = dim.pagerIndicatorHeight
				),
				shape = CircleShape,
				color = if (current == index) activeColor else inactiveColor
			) {}
		}
	}
}

@Composable
fun BoxScope.AppBottomScrimInfo(
	content: @Composable ColumnScope.() -> Unit
) {
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
