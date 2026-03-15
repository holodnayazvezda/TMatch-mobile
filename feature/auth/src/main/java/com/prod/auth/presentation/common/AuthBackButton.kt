package com.prod.auth.presentation.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.prod.auth.R

@Composable
fun AuthBackButton(
	onClick: () -> Unit,
	modifier: Modifier = Modifier
) {
	Row(
		modifier = modifier
			.clip(RoundedCornerShape(16.dp))
			.clickable(onClick = onClick)
			.defaultMinSize(minWidth = 96.dp, minHeight = 48.dp)
			.padding(vertical = 12.dp),
		verticalAlignment = Alignment.CenterVertically
	) {
		Icon(
			painter = painterResource(id = R.drawable.back),
			contentDescription = null,
			tint = MaterialTheme.colorScheme.onBackground
		)
		Spacer(modifier = Modifier.width(8.dp))
		Text(
			text = stringResource(R.string.auth_back),
			style = MaterialTheme.typography.labelLarge,
			color = MaterialTheme.colorScheme.onBackground
		)
	}
}
