package com.prod.auth.presentation.signup.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.prod.auth.R
import com.prod.ui.theme.LocalDimensions
import com.prod.core.ui.theme.LocalShapes

@Composable
fun SignUpJuryShortcutButton(
	onClick: () -> Unit,
	enabled: Boolean,
	modifier: Modifier = Modifier
) {
	val dim = LocalDimensions.current
	val shapes = LocalShapes.current

	FilledTonalButton(
		onClick = onClick,
		enabled = enabled,
		modifier = modifier,
		shape = shapes.pillRounded,
		contentPadding = PaddingValues(
			horizontal = dim.pillHorizontalPadding,
			vertical = dim.pillVerticalPadding + dim.extraSmall / 2
		)
	) {
		Text(
			text = stringResource(R.string.auth_sign_up_jury_shortcut),
			style = MaterialTheme.typography.labelLarge
		)
	}
}
