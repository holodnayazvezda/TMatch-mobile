package com.prod.tmatch_api.auth.data.gateway

import android.content.Context
import android.content.SharedPreferences
import com.prod.auth.domain.gateway.AuthSessionStore
import com.prod.auth.domain.model.AuthSession

class SharedPrefsAuthSessionStore(
	context: Context
) : AuthSessionStore {

	private val preferences: SharedPreferences =
		context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

	override fun get(): AuthSession? {
		val accessToken = preferences.getString(KEY_ACCESS_TOKEN, null)
		val userId = preferences.getString(KEY_USER_ID, null)

		if (accessToken.isNullOrBlank() || userId.isNullOrBlank()) {
			return null
		}

		return AuthSession(
			accessToken = accessToken,
			userId = userId
		)
	}

	override fun save(session: AuthSession) {
		preferences.edit()
			.putString(KEY_ACCESS_TOKEN, session.accessToken)
			.putString(KEY_USER_ID, session.userId)
			.apply()
	}

	override fun clear() {
		preferences.edit()
			.remove(KEY_ACCESS_TOKEN)
			.remove(KEY_USER_ID)
			.apply()
	}

	private companion object {
		const val PREFERENCES_NAME = "auth_session"
		const val KEY_ACCESS_TOKEN = "access_token"
		const val KEY_USER_ID = "user_id"
	}
}
