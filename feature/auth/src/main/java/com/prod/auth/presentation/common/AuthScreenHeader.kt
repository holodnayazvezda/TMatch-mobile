package com.prod.auth.presentation.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AuthScreenHeader(
	title: String,
	description: String,
	onBack: () -> Unit,
	modifier: Modifier = Modifier
) {
	Column(modifier = modifier) {
		AuthBackButton(onClick = onBack)
		Spacer(modifier = Modifier.height(20.dp))
		AuthScreenHeadlineTitle(title = title)
		Spacer(modifier = Modifier.height(8.dp))
		AuthScreenHeadlineDescription(description = description)
	}
}
