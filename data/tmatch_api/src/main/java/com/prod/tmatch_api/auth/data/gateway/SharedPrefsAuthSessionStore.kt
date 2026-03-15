package com.prod.tmatch_api.auth.data.gateway

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.prod.auth.domain.gateway.AuthSessionStore
import com.prod.common.api.model.UserSession

class SharedPrefsAuthSessionStore(
	context: Context
) : AuthSessionStore {

	private val preferences: SharedPreferences = EncryptedSharedPreferences.create(
		context,
		PREFERENCES_NAME,
		MasterKey.Builder(context)
			.setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
			.build(),
		EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
		EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
	)

	override fun get(): UserSession? {
		val accessToken = preferences.getString(KEY_ACCESS_TOKEN, null)
		val userId = preferences.getString(KEY_USER_ID, null)

		if (accessToken.isNullOrBlank() || userId.isNullOrBlank()) {
			return null
		}

		return UserSession(
			accessToken = accessToken,
			userId = userId
		)
	}

	override fun save(session: UserSession) {
		preferences.edit()
			.putString(KEY_ACCESS_TOKEN, session.accessToken)
			.putString(KEY_USER_ID, session.userId)
			.apply()
	}

	override fun isProfileCompleted(): Boolean {
		if (get() == null) {
			return false
		}

		return if (preferences.contains(KEY_PROFILE_COMPLETED)) {
			preferences.getBoolean(KEY_PROFILE_COMPLETED, false)
		} else {
			false
		}
	}

	override fun setProfileCompleted(isCompleted: Boolean) {
		preferences.edit()
			.putBoolean(KEY_PROFILE_COMPLETED, isCompleted)
			.apply()
	}

	override fun clear() {
		preferences.edit()
			.remove(KEY_ACCESS_TOKEN)
			.remove(KEY_USER_ID)
			.remove(KEY_PROFILE_COMPLETED)
			.apply()
	}

	private companion object {
		const val PREFERENCES_NAME = "auth_session"
		const val KEY_ACCESS_TOKEN = "access_token"
		const val KEY_USER_ID = "user_id"
		const val KEY_PROFILE_COMPLETED = "profile_completed"
	}
}
