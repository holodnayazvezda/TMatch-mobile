package com.prod.auth.domain.validation

import android.util.Patterns

class EmailValidator {
	fun validate(email: String): String? {
		if (email.isBlank()) {
			return "Введите email."
		}
		if (!Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
			return "Введите корректный email."
		}
		return null
	}
}
