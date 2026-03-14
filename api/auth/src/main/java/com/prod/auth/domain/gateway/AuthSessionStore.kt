package com.prod.auth.domain.gateway

import com.prod.common.api.model.UserSession

interface AuthSessionStore {
	fun get(): UserSession?
	fun save(session: UserSession)
	fun clear()
}
