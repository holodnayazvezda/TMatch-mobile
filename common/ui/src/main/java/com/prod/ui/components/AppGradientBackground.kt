package com.prod.ui.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush

@Composable
fun rememberAppGradientBrush(): Brush {
	val colorScheme = MaterialTheme.colorScheme
	val isDarkTheme = isSystemInDarkTheme()
	val gradientColors = if (isDarkTheme) {
		listOf(
			colorScheme.primaryContainer.copy(alpha = 0.65f),
			colorScheme.background,
			colorScheme.surfaceContainerLow
		)
	} else {
		listOf(
			colorScheme.primaryContainer,
			colorScheme.secondaryContainer.copy(alpha = 0.95f),
			colorScheme.tertiaryContainer.copy(alpha = 0.78f),
			colorScheme.surface.copy(alpha = 0.92f)
		)
	}

	return Brush.verticalGradient(colors = gradientColors)
}
