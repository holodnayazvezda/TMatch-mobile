package com.prod.core.ui.theme

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class Dimensions(
	val extraSmall: Dp = 4.dp,
	val verySmall: Dp = 8.dp,
	val small: Dp = 12.dp,
	val medium: Dp = 20.dp,
	val large: Dp = 24.dp
)

val LocalDimensions = compositionLocalOf { Dimensions() }