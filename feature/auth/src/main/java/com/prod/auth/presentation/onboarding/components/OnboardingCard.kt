package com.prod.auth.presentation.onboarding.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.prod.auth.presentation.onboarding.model.OnboardingPageUiModel

@Composable
fun OnboardingCard(
	page: OnboardingPageUiModel,
	modifier: Modifier = Modifier
) {
	Surface(
		modifier = modifier.fillMaxSize(),
		shape = RoundedCornerShape(32.dp),
		color = MaterialTheme.colorScheme.surface,
		tonalElevation = 6.dp
	) {
		Column(
			modifier = Modifier
				.fillMaxSize()
				.padding(horizontal = 24.dp, vertical = 28.dp)
		) {
			Surface(
				color = MaterialTheme.colorScheme.primaryContainer,
				contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
				shape = RoundedCornerShape(999.dp)
			) {
				Text(
					text = stringResource(page.labelRes),
					style = MaterialTheme.typography.labelLarge,
					modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
				)
			}
			Spacer(modifier = Modifier.height(30.dp))
			Text(
				text = stringResource(page.titleRes),
				style = MaterialTheme.typography.headlineMedium,
				color = MaterialTheme.colorScheme.onSurface,
				fontWeight = FontWeight.SemiBold
			)
			Spacer(modifier = Modifier.height(28.dp))
			Text(
				text = stringResource(page.descriptionRes),
				style = MaterialTheme.typography.bodyLarge,
				color = MaterialTheme.colorScheme.onSurfaceVariant
			)
			Spacer(modifier = Modifier.weight(1f))
			Spacer(modifier = Modifier.height(24.dp))
		}
	}
}
