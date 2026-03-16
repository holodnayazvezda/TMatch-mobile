package com.prod.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prod.ui.theme.LocalDimensions
import com.prod.core.ui.theme.LocalShapes
import com.prod.ui.theme.LocalTmatchExtraColors
import com.prod.ui.R

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
fun AppScrollableScreenWithHeader(
	modifier: Modifier = Modifier,
	header: @Composable ColumnScope.() -> Unit,
	content: @Composable ColumnScope.() -> Unit
) {
	val dim = LocalDimensions.current

	Column(
		modifier = modifier
			.fillMaxSize()
			.safeDrawingPadding()
			.padding(
				horizontal = dim.large,
				vertical = dim.screenVerticalPadding
			),
		verticalArrangement = Arrangement.spacedBy(dim.defaultSpacing)
	) {
		Column(
			modifier = Modifier.fillMaxWidth(),
			verticalArrangement = Arrangement.spacedBy(dim.small),
			content = header
		)
		Column(
			modifier = Modifier
				.fillMaxWidth()
				.weight(1f)
				.verticalScroll(rememberScrollState()),
			verticalArrangement = Arrangement.spacedBy(dim.defaultSpacing),
			content = content
		)
	}
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
	titleFontSize: TextUnit = 22.sp,
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
			style = MaterialTheme.typography.titleLarge.copy(fontSize = titleFontSize),
			color = MaterialTheme.colorScheme.onBackground,
			textAlign = TextAlign.Center,
			maxLines = 1,
			overflow = TextOverflow.Ellipsis
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
fun AppTopBarIconAction(
	iconRes: Int,
	contentDescription: String,
	onClick: () -> Unit,
	modifier: Modifier = Modifier,
	enabled: Boolean = true
) {
	val dim = LocalDimensions.current

	IconButton(
		onClick = onClick,
		enabled = enabled,
		modifier = modifier
	) {
		Icon(
			painter = painterResource(iconRes),
			contentDescription = contentDescription,
			modifier = Modifier.size(dim.iconMedium),
			tint = MaterialTheme.colorScheme.onBackground
		)
	}
}

@Composable
fun AppBackButton(
	text: String,
	onClick: () -> Unit,
	modifier: Modifier = Modifier
) {
	Row(
		modifier = modifier
			.clip(RoundedCornerShape(16.dp))
			.clickable(onClick = onClick)
			.defaultMinSize(minWidth = 96.dp, minHeight = 48.dp)
			.padding(vertical = 12.dp),
		verticalAlignment = Alignment.CenterVertically
	) {
		Icon(
			painter = painterResource(id = R.drawable.back),
			contentDescription = null,
			tint = MaterialTheme.colorScheme.onBackground
		)
		Spacer(modifier = Modifier.width(8.dp))
		Text(
			text = text,
			style = MaterialTheme.typography.labelLarge,
			color = MaterialTheme.colorScheme.onBackground
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
