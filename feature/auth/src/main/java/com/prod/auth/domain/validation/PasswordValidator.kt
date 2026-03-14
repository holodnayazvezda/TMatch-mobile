package com.prod.auth.domain.validation

import com.prod.auth.R
import com.prod.auth.presentation.common.UiText

class PasswordValidator {
	fun validateForSignIn(password: String): UiText? {
		return if (password.isBlank()) {
			UiText.StringResource(R.string.validation_password_empty)
		} else {
			null
		}
	}

	fun validateForSignUp(password: String): UiText? {
		if (password.isBlank()) {
			return UiText.StringResource(R.string.validation_password_empty)
		}
		if (password.length < 8) {
			return UiText.StringResource(R.string.validation_password_too_short)
		}
		return null
	}
}
