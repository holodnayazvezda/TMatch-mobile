package com.prod.auth.domain.validation

class PasswordValidator {
	fun validateForSignIn(password: String): String? {
		return if (password.isBlank()) {
			"Введите пароль."
		} else {
			null
		}
	}

	fun validateForSignUp(password: String): String? {
		if (password.isBlank()) {
			return "Введите пароль."
		}
		if (password.length < 8) {
			return "Пароль должен содержать минимум 8 символов."
		}
		return null
	}
}
