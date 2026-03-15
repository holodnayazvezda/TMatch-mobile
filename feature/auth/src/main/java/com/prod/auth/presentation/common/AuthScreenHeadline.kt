package com.prod.auth.presentation.common

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun AuthScreenHeadline(
	title: String,
	description: String
) {
	AuthScreenHeadlineTitle(title = title)
	Spacer(modifier = Modifier.height(8.dp))
	AuthScreenHeadlineDescription(description = description)
}

@Composable
fun AuthScreenHeadlineTitle(
	title: String,
	modifier: Modifier = Modifier
) {
	Text(
		modifier = modifier,
		text = title,
		style = MaterialTheme.typography.headlineMedium,
		color = MaterialTheme.colorScheme.onBackground,
		fontWeight = FontWeight.SemiBold,
		textAlign = TextAlign.Center
	)
}

@Composable
fun AuthScreenHeadlineDescription(description: String) {
	Text(
		text = description,
		style = MaterialTheme.typography.bodyLarge,
		color = MaterialTheme.colorScheme.onSurfaceVariant
	)
}
