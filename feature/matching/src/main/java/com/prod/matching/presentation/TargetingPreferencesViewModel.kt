package com.prod.matching.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prod.common.api.model.ApiErrorReason
import com.prod.common.api.model.ApiOperationResult
import com.prod.common.api.model.sanitizeUserFacingApiMessage
import com.prod.match.domain.model.TargetGender
import com.prod.match.domain.model.TargetingPreferencesDraft
import com.prod.matching.domain.usecase.GetOwnTargetingUseCase
import com.prod.matching.domain.usecase.UpsertOwnTargetingUseCase
import kotlinx.coroutines.launch

class TargetingPreferencesViewModel(
	private val getOwnTargetingUseCase: GetOwnTargetingUseCase,
	private val upsertOwnTargetingUseCase: UpsertOwnTargetingUseCase
) : ViewModel() {

	private var isInitialized = false
	private var unauthorizedHandler: (() -> Unit)? = null
	private var noInternetHandler: (((() -> Unit)) -> Unit)? = null

	var uiState by mutableStateOf(TargetingPreferencesUiState())
		private set

	fun initialize(
		onUnauthorized: () -> Unit,
		onNoInternet: (retryAction: () -> Unit) -> Unit
	) {
		unauthorizedHandler = onUnauthorized
		noInternetHandler = onNoInternet
		if (isInitialized) {
			return
		}

		isInitialized = true
		loadTargeting()
	}

	fun retry() {
		loadTargeting()
	}

	fun onGenderTargetSelected(genderTarget: TargetGender) {
		uiState = uiState.copy(genderTarget = genderTarget, error = null)
	}

	fun onRegionSelected(region: String?) {
		uiState = uiState.copy(region = region, error = null)
	}

	fun onAgeRangeChanged(
		ageFrom: Int,
		ageTo: Int
	) {
		uiState = uiState.copy(
			ageFrom = ageFrom.coerceAtLeast(18),
			ageTo = ageTo.coerceAtLeast(ageFrom).coerceAtMost(80),
			error = null
		)
	}

	fun save(onSaved: () -> Unit) {
		if (uiState.isLoading || uiState.isSaving) {
			return
		}

		viewModelScope.launch {
			uiState = uiState.copy(isSaving = true, error = null)
			when (
				val result = upsertOwnTargetingUseCase(
					TargetingPreferencesDraft(
						region = uiState.region,
						genderTarget = uiState.genderTarget,
						ageFrom = uiState.ageFrom,
						ageTo = uiState.ageTo
					)
				)
			) {
				is ApiOperationResult.Success -> {
					uiState = uiState.copy(
						isSaving = false,
						error = null,
						region = result.data.region,
						genderTarget = result.data.genderTarget,
						ageFrom = result.data.ageFrom,
						ageTo = result.data.ageTo
					)
					onSaved()
				}

				is ApiOperationResult.ValidationError -> {
					uiState = uiState.copy(
						isSaving = false,
						error = result.message.orDefaultErrorMessage()
					)
				}

				is ApiOperationResult.ApiError -> {
					if (result.reason == ApiErrorReason.Unauthorized) {
						uiState = uiState.copy(isSaving = false, error = null)
						unauthorizedHandler?.invoke()
						return@launch
					}

					uiState = uiState.copy(
						isSaving = false,
						error = result.toMessage()
					)
				}

				ApiOperationResult.NoInternet -> {
					uiState = uiState.copy(isSaving = false, error = null)
					noInternetHandler?.invoke {
						save(onSaved)
					}
				}
			}
		}
	}

	private fun loadTargeting() {
		viewModelScope.launch {
			uiState = uiState.copy(isLoading = true, error = null)
			when (val result = getOwnTargetingUseCase()) {
				is ApiOperationResult.Success -> {
					uiState = uiState.copy(
						isLoading = false,
						error = null,
						region = result.data.region,
						genderTarget = result.data.genderTarget,
						ageFrom = result.data.ageFrom,
						ageTo = result.data.ageTo
					)
				}

				is ApiOperationResult.ValidationError -> {
					uiState = uiState.copy(
						isLoading = false,
						error = result.message.orDefaultErrorMessage()
					)
				}

				is ApiOperationResult.ApiError -> {
					if (result.reason == ApiErrorReason.Unauthorized) {
						uiState = uiState.copy(isLoading = false, error = null)
						unauthorizedHandler?.invoke()
						return@launch
					}

					uiState = uiState.copy(
						isLoading = false,
						error = result.toMessage()
					)
				}

				ApiOperationResult.NoInternet -> {
					uiState = uiState.copy(isLoading = false, error = null)
					noInternetHandler?.invoke {
						loadTargeting()
					}
				}
			}
		}
	}

	private fun ApiOperationResult.ApiError.toMessage(): String {
		return message.orDefaultErrorMessage(
			fallback = when (reason) {
				ApiErrorReason.Unauthorized -> "Сессия истекла, войдите снова"
				ApiErrorReason.ServiceUnavailable -> "Сервис временно недоступен"
				ApiErrorReason.RequestFailed -> "Не удалось сохранить предпочтения"
			}
		)
	}

	private fun String?.orDefaultErrorMessage(
		fallback: String = "Произошла ошибка"
	): String {
		return sanitizeUserFacingApiMessage() ?: fallback
	}
}
