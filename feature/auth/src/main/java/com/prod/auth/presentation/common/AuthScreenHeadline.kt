package com.prod.auth.presentation.common

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun AuthScreenHeadline(
	title: String,
	description: String
) {
	Text(
		text = title,
		style = MaterialTheme.typography.headlineMedium,
		color = MaterialTheme.colorScheme.onBackground,
		fontWeight = FontWeight.SemiBold
	)
	Spacer(modifier = androidx.compose.ui.Modifier.height(8.dp))
	Text(
		text = description,
		style = MaterialTheme.typography.bodyLarge,
		color = MaterialTheme.colorScheme.onSurfaceVariant
	)
}
