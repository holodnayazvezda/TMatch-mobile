package com.prod.t_match.presentation.blank

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.prod.ui.components.AppGradientBackground
import org.koin.androidx.compose.koinViewModel

@Composable
fun BlankScreen(
	viewModel: BlankViewModel = koinViewModel()
) {
	val uiState = viewModel.uiState

	AppGradientBackground {
		Surface(
			modifier = Modifier.fillMaxSize(),
			color = MaterialTheme.colorScheme.background.copy(alpha = if (uiState.isReady) 0f else 0f)
		) {
			Box(modifier = Modifier.fillMaxSize())
		}
	}
}
