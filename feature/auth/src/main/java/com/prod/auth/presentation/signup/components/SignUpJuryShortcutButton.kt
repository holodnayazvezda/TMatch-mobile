package com.prod.auth.presentation.signup.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.prod.auth.R

@Composable
fun SignUpJuryShortcutButton(
	onClick: () -> Unit,
	enabled: Boolean,
	modifier: Modifier = Modifier
) {
	FilledTonalButton(
		onClick = onClick,
		enabled = enabled,
		modifier = modifier,
		shape = RoundedCornerShape(999.dp),
		contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
	) {
		Text(
			text = stringResource(R.string.auth_sign_up_jury_shortcut),
			style = MaterialTheme.typography.labelLarge
		)
	}
}
