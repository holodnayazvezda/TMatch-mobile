package com.prod.auth.domain.gateway

import com.prod.auth.domain.model.AuthSession

interface AuthSessionStore {
	fun get(): AuthSession?
	fun save(session: AuthSession)
	fun clear()
}
