package com.prod.auth.domain.gateway

import com.prod.common.api.model.UserSession

interface AuthSessionStore {
	fun get(): UserSession?
	fun save(session: UserSession)
	fun isProfileCompleted(): Boolean
	fun setProfileCompleted(isCompleted: Boolean)
	fun hasDatingProfile(): Boolean
	fun setHasDatingProfile(hasDatingProfile: Boolean)
	fun clear()
}
