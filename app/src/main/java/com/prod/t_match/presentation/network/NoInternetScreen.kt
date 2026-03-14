package com.prod.t_match.presentation.network

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.prod.ui.components.buttons.AppBigButton

@Composable
fun NoInternetScreen(
	onRetry: () -> Unit,
	viewModel: NoInternetViewModel = viewModel()
) {
	val uiState = viewModel.uiState

	Box(
		modifier = Modifier
			.fillMaxSize()
			.background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.55f)),
		contentAlignment = Alignment.Center
	) {
		Surface(
			modifier = Modifier.padding(24.dp),
			shape = RoundedCornerShape(28.dp),
			color = MaterialTheme.colorScheme.surface,
			tonalElevation = 10.dp
		) {
			Column(
				modifier = Modifier.padding(24.dp),
				verticalArrangement = Arrangement.spacedBy(16.dp)
			) {
				Text(
					text = uiState.title,
					style = MaterialTheme.typography.headlineSmall,
					color = MaterialTheme.colorScheme.onSurface
				)
				Text(
					text = uiState.description,
					style = MaterialTheme.typography.bodyLarge,
					color = MaterialTheme.colorScheme.onSurfaceVariant
				)
				AppBigButton(
					buttonText = uiState.retryButtonLabel,
					isLoading = false,
					onClick = onRetry
				)
			}
		}
	}
}
