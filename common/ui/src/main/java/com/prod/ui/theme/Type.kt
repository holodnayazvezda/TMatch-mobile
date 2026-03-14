package com.prod.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val AppFontFamily = FontFamily.Default

val Typography = Typography(
	titleLarge = TextStyle(
		fontFamily = AppFontFamily,
		fontWeight = FontWeight.Bold,
		fontSize = 24.sp
	),
	titleMedium = TextStyle(
		fontFamily = AppFontFamily,
		fontWeight = FontWeight.Medium,
		fontSize = 18.sp
	),
	labelLarge = TextStyle(
		fontFamily = AppFontFamily,
		fontWeight = FontWeight.Normal,
		fontSize = 16.sp
	)
)
