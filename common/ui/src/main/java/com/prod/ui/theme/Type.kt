package com.prod.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.prod.ui.R

private val AppFontFamily = FontFamily(
	Font(R.font.tinkoff_sans_regular, FontWeight.Normal),
	Font(R.font.tinkoff_sans_medium, FontWeight.Medium),
	Font(R.font.tinkoff_sans_bold, FontWeight.SemiBold),
	Font(R.font.tinkoff_sans_bold, FontWeight.Bold)
)

private val BaseTypography = Typography()

private fun TextStyle.withAppFont(
	fontWeight: FontWeight? = null
): TextStyle = copy(
	fontFamily = AppFontFamily,
	fontWeight = fontWeight ?: this.fontWeight
)

val Typography = Typography(
	displayLarge = BaseTypography.displayLarge.withAppFont(),
	displayMedium = BaseTypography.displayMedium.withAppFont(),
	displaySmall = BaseTypography.displaySmall.withAppFont(),
	headlineLarge = BaseTypography.headlineLarge.withAppFont(),
	headlineMedium = BaseTypography.headlineMedium.withAppFont(),
	headlineSmall = BaseTypography.headlineSmall.withAppFont(),
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
	titleSmall = BaseTypography.titleSmall.withAppFont(),
	bodyLarge = BaseTypography.bodyLarge.withAppFont(),
	bodyMedium = BaseTypography.bodyMedium.withAppFont(),
	bodySmall = BaseTypography.bodySmall.withAppFont(),
	labelLarge = TextStyle(
		fontFamily = AppFontFamily,
		fontWeight = FontWeight.Normal,
		fontSize = 16.sp
	),
	labelMedium = BaseTypography.labelMedium.withAppFont(),
	labelSmall = BaseTypography.labelSmall.withAppFont()
)
