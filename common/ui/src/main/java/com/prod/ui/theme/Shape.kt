package com.prod.core.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class Shapes(
	val veryBigRoundedValue: Dp = 32.dp,
	val largeRounded: RoundedCornerShape = RoundedCornerShape(28.dp),
	val sectionRounded: RoundedCornerShape = RoundedCornerShape(22.dp),
	val mediumRounded: RoundedCornerShape = RoundedCornerShape(18.dp),
	val buttonRounded: RoundedCornerShape = RoundedCornerShape(14.dp),
	val pillRounded: RoundedCornerShape = RoundedCornerShape(999.dp),
	val smallRounded: RoundedCornerShape = RoundedCornerShape(6.dp)
)

val LocalShapes = compositionLocalOf { Shapes() }
