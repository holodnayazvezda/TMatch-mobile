package com.prod.matching.domain.interactor

import com.prod.common.api.model.ApiOperationResult
import com.prod.matching.domain.gateway.PendingMatchActionStore
import com.prod.matching.domain.model.PendingMatchAction
import java.util.ArrayDeque

class PendingMatchActionQueue(
	private val pendingMatchActionStore: PendingMatchActionStore
) {
	private val pendingActions = ArrayDeque<PendingMatchAction>()

	fun restore() {
		pendingActions.clear()
		pendingActions.addAll(pendingMatchActionStore.getAll())
	}

	fun enqueue(action: PendingMatchAction) {
		pendingActions.addLast(action)
		sync()
	}

	fun isEmpty(): Boolean = pendingActions.isEmpty()

	fun clear() {
		pendingActions.clear()
		sync()
	}

	suspend fun drain(
		submit: suspend (PendingMatchAction) -> ApiOperationResult<*>?
	): DrainResult {
		while (pendingActions.isNotEmpty()) {
			val currentAction = pendingActions.first()
			when (val result = submit(currentAction)) {
				null,
				is ApiOperationResult.Success -> {
					pendingActions.removeFirst()
					sync()
				}

				is ApiOperationResult.ValidationError -> {
					pendingActions.removeFirst()
					sync()
					return DrainResult.ValidationError(result.message)
				}

				is ApiOperationResult.ApiError -> {
					return DrainResult.ApiError(result)
				}

				ApiOperationResult.NoInternet -> {
					return DrainResult.NoInternet
				}
			}
		}

		return DrainResult.Completed
	}

	private fun sync() {
		if (pendingActions.isEmpty()) {
			pendingMatchActionStore.clear()
			return
		}

		pendingMatchActionStore.saveAll(pendingActions.toList())
	}
}

sealed interface DrainResult {
	data object Completed : DrainResult
	data object NoInternet : DrainResult
	data class ValidationError(val message: String?) : DrainResult
	data class ApiError(val result: ApiOperationResult.ApiError) : DrainResult
}
