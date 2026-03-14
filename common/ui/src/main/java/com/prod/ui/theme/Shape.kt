package com.prod.core.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class Shapes(
	val veryBigRoundedValue: Dp = 32.dp,
	val mediumRounded: RoundedCornerShape = RoundedCornerShape(18.dp),
	val smallRounded: RoundedCornerShape = RoundedCornerShape(6.dp)
)

val LocalShapes = compositionLocalOf { Shapes() }