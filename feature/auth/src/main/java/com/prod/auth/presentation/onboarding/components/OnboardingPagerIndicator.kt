package com.prod.auth.presentation.onboarding.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun OnboardingPagerIndicator(
	total: Int,
	current: Int,
	modifier: Modifier = Modifier
) {
	Row(
		modifier = modifier.fillMaxWidth(),
		horizontalArrangement = Arrangement.Center
	) {
		repeat(total) { index ->
			val isSelected = index == current
			Box(
				modifier = Modifier
					.padding(horizontal = 4.dp)
					.height(8.dp)
					.width(if (isSelected) 32.dp else 10.dp)
					.background(
						color = if (isSelected) {
							MaterialTheme.colorScheme.primary
						} else {
							MaterialTheme.colorScheme.outlineVariant
						},
						shape = RoundedCornerShape(999.dp)
					)
			)
		}
	}
}
