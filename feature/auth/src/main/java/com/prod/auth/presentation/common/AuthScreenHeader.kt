package com.prod.auth.presentation.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.prod.ui.components.AppBackButton
import com.prod.ui.components.AppScreenTopBar
import com.prod.auth.R

@Composable
fun AuthScreenHeader(
	title: String,
	description: String,
	onBack: () -> Unit,
	modifier: Modifier = Modifier
) {
	Column(modifier = modifier) {
		AppScreenTopBar(
			title = "",
			horizontalPadding = 0.dp,
			applySafeDrawingPadding = false,
			navigation = {
				AppBackButton(
					text = stringResource(R.string.auth_back),
					onClick = onBack
				)
			}
		)
		Spacer(modifier = Modifier.height(8.dp))
		AuthScreenHeadlineTitle(title = title)
		Spacer(modifier = Modifier.height(8.dp))
		AuthScreenHeadlineDescription(description = description)
	}
}
