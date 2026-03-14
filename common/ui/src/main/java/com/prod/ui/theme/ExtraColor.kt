package com.prod.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class TmatchExtraColors(
	val accentGold: Color,
	val baseScrim: Color,
	val onBaseScrim: Color
)

private val BaseExtraColors = TmatchExtraColors(
	accentGold = Color(0xFFD4A84B),
	baseScrim = Color.Black.copy(0.3f),
	onBaseScrim = Color.White
)

internal val LightExtraColors = BaseExtraColors
internal val DarkExtraColors = BaseExtraColors

val LocalTmatchExtraColors = staticCompositionLocalOf { DarkExtraColors }
