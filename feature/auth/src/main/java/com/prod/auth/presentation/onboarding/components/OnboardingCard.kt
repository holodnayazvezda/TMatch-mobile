package com.prod.auth.presentation.onboarding.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.prod.auth.presentation.onboarding.model.OnboardingPageUiModel
import com.prod.core.ui.theme.LocalDimensions
import com.prod.ui.components.AppInfoChip
import com.prod.ui.components.AppSectionCard

@Composable
fun OnboardingCard(
	page: OnboardingPageUiModel,
	modifier: Modifier = Modifier
) {
	val dim = LocalDimensions.current

	AppSectionCard(
		modifier = modifier.fillMaxSize(),
		shape = com.prod.core.ui.theme.LocalShapes.current.largeRounded,
		tonalElevation = dim.extraSmall + dim.extraSmall / 2
	) {
		Column(
			modifier = Modifier.fillMaxSize()
		) {
			AppInfoChip(
				text = stringResource(page.labelRes),
				containerColor = MaterialTheme.colorScheme.primaryContainer,
				contentColor = MaterialTheme.colorScheme.onPrimaryContainer
			)
			Spacer(modifier = Modifier.height(dim.large + dim.extraSmall / 2))
			Text(
				text = stringResource(page.titleRes),
				style = MaterialTheme.typography.headlineMedium,
				color = MaterialTheme.colorScheme.onSurface,
				fontWeight = FontWeight.SemiBold
			)
			Spacer(modifier = Modifier.height(dim.large + dim.extraSmall))
			Text(
				text = stringResource(page.descriptionRes),
				style = MaterialTheme.typography.bodyLarge,
				color = MaterialTheme.colorScheme.onSurfaceVariant
			)
			Spacer(modifier = Modifier.weight(1f))
		}
	}
}
