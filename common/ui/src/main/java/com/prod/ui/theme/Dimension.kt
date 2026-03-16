package com.prod.core.ui.theme

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class Dimensions(
	val extraSmall: Dp = 4.dp,
	val verySmall: Dp = 8.dp,
	val small: Dp = 12.dp,
	val defaultSpacing: Dp = 16.dp,
	val medium: Dp = 20.dp,
	val large: Dp = 24.dp,
	val extraLarge: Dp = 32.dp,
	val screenHorizontalPadding: Dp = 20.dp,
	val screenVerticalPadding: Dp = 8.dp,
	val sectionHorizontalPadding: Dp = 18.dp,
	val sectionVerticalPadding: Dp = 16.dp,
	val sectionCornerRadius: Dp = 22.dp,
	val factCardCornerRadius: Dp = 18.dp,
	val heroCornerRadius: Dp = 28.dp,
	val pillHorizontalPadding: Dp = 14.dp,
	val pillVerticalPadding: Dp = 8.dp,
	val pagerIndicatorInset: Dp = 18.dp,
	val pagerIndicatorGap: Dp = 6.dp,
	val pagerIndicatorActiveWidth: Dp = 18.dp,
	val pagerIndicatorInactiveWidth: Dp = 8.dp,
	val pagerIndicatorHeight: Dp = 8.dp,
	val iconSmall: Dp = 20.dp,
	val iconMedium: Dp = 24.dp,
	val iconContainer: Dp = 36.dp,
	val largeIconContainer: Dp = 72.dp,
	val gridGap: Dp = 42.dp,
	val cardOuterPadding: Dp = 6.dp,
	val cardStackInset: Dp = 10.dp,
	val cardStackVerticalInset: Dp = 18.dp,
	val sheetHandleHeight: Dp = 5.dp,
	val actionButtonHeight: Dp = 56.dp
)

val LocalDimensions = compositionLocalOf { Dimensions() }
