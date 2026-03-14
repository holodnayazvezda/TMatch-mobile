package com.prod.auth.domain.validation

import com.prod.auth.R
import com.prod.auth.presentation.common.UiText

class ConfirmPasswordValidator {
	fun validate(password: String, confirmPassword: String): UiText? {
		if (confirmPassword.isBlank()) {
			return UiText.StringResource(R.string.validation_confirm_password_empty)
		}
		if (password != confirmPassword) {
			return UiText.StringResource(R.string.validation_confirm_password_mismatch)
		}
		return null
	}
}
