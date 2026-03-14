package com.prod.auth.domain.validation

import android.util.Patterns
import com.prod.auth.R
import com.prod.auth.presentation.common.UiText

class EmailValidator {
	fun validate(email: String): UiText? {
		if (email.isBlank()) {
			return UiText.StringResource(R.string.validation_email_empty)
		}
		if (!Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
			return UiText.StringResource(R.string.validation_email_invalid)
		}
		return null
	}
}
