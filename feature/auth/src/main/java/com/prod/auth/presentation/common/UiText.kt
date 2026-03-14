package com.prod.auth.presentation.common

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

sealed interface UiText {
	data class DynamicString(val value: String) : UiText
	data class StringResource(@param:StringRes val resId: Int) : UiText
}

@Composable
fun UiText.asString(): String {
	return when (this) {
		is UiText.DynamicString -> value
		is UiText.StringResource -> stringResource(resId)
	}
}

@Composable
fun UiText?.asStringOrNull(): String? = this?.asString()
