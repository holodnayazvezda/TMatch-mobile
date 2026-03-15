package com.prod.matching.domain.gateway

import com.prod.matching.domain.model.PendingMatchAction

interface PendingMatchActionStore {
	fun getAll(): List<PendingMatchAction>
	fun saveAll(actions: List<PendingMatchAction>)
	fun clear()
}
