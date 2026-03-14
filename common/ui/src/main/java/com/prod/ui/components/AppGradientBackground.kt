package com.prod.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush

@Composable
fun AppGradientBackground(
	modifier: Modifier = Modifier,
	content: @Composable () -> Unit
) {
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

	Box(
		modifier = modifier
			.fillMaxSize()
			.background(
				brush = Brush.verticalGradient(colors = gradientColors)
			)
	) {
		content()
	}
}
