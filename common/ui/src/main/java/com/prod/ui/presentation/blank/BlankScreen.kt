package com.prod.ui.presentation.blank

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.prod.ui.components.AppGradientBackground

@Composable
fun BlankScreen() {
	AppGradientBackground {
		Surface(
			modifier = Modifier.fillMaxSize(),
			color = MaterialTheme.colorScheme.background.copy(alpha = 0f)
		) {
			Box(modifier = Modifier.fillMaxSize())
		}
	}
}
