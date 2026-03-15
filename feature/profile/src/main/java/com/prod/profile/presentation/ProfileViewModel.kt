package com.prod.profile.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prod.common.api.model.ApiErrorReason
import com.prod.common.api.model.ApiOperationResult
import com.prod.profile.domain.usecase.CreateOwnDatingProfileUseCase
import com.prod.profile.domain.usecase.GetOwnDatingProfileUseCase
import com.prod.profile.domain.usecase.UploadDatingPhotoUseCase
import kotlinx.coroutines.launch

class ProfileViewModel(
	private val getOwnDatingProfileUseCase: GetOwnDatingProfileUseCase,
	private val createOwnDatingProfileUseCase: CreateOwnDatingProfileUseCase,
	private val uploadDatingPhotoUseCase: UploadDatingPhotoUseCase
) : ViewModel() {

	var uiState by mutableStateOf(ProfileUiState())
		private set

	private var isInitialized = false

	fun initialize(
		onUnauthorized: () -> Unit,
		onNoInternet: (retryAction: () -> Unit) -> Unit
	) {
		if (isInitialized) return
		isInitialized = true
		ensureDatingProfile(
			onUnauthorized = onUnauthorized,
			onNoInternet = onNoInternet
		)
	}

	fun retry(
		onUnauthorized: () -> Unit,
		onNoInternet: (retryAction: () -> Unit) -> Unit
	) {
		ensureDatingProfile(
			onUnauthorized = onUnauthorized,
			onNoInternet = onNoInternet
		)
	}

	fun onPhotoRequired() {
		uiState = uiState.copy(error = "Выберите фото для dating profile")
	}

	fun createDatingProfile(
		imageBytes: ByteArray,
		onUnauthorized: () -> Unit,
		onNoInternet: (retryAction: () -> Unit) -> Unit
	) {
		viewModelScope.launch {
			uiState = uiState.copy(isSubmitting = true, error = null)

			when (
				val uploadResult = uploadDatingPhotoUseCase(
					contentType = "image/png",
					bytes = imageBytes
				)
			) {
				is ApiOperationResult.Success -> {
					when (val createResult = createOwnDatingProfileUseCase(photoUrl = uploadResult.data)) {
						is ApiOperationResult.Success -> {
							uiState = uiState.copy(
								isLoading = false,
								isSubmitting = false,
								requiresDatingPhoto = false,
								message = "Dating profile создан",
								error = null
							)
						}

						is ApiOperationResult.ValidationError -> {
							uiState = uiState.copy(
								isLoading = false,
								isSubmitting = false,
								error = createResult.message.orDefaultErrorMessage()
							)
						}

						is ApiOperationResult.ApiError -> {
							if (createResult.reason == ApiErrorReason.Unauthorized) {
								uiState = uiState.copy(isLoading = false, isSubmitting = false)
								onUnauthorized()
								return@launch
							}

							uiState = uiState.copy(
								isLoading = false,
								isSubmitting = false,
								error = createResult.toMessage()
							)
						}

						ApiOperationResult.NoInternet -> {
							uiState = uiState.copy(isLoading = false, isSubmitting = false)
							onNoInternet {
								createDatingProfile(
									imageBytes = imageBytes,
									onUnauthorized = onUnauthorized,
									onNoInternet = onNoInternet
								)
							}
						}
					}
				}

				is ApiOperationResult.ValidationError -> {
					uiState = uiState.copy(
						isLoading = false,
						isSubmitting = false,
						error = uploadResult.message.orDefaultErrorMessage()
					)
				}

				is ApiOperationResult.ApiError -> {
					if (uploadResult.reason == ApiErrorReason.Unauthorized) {
						uiState = uiState.copy(isLoading = false, isSubmitting = false)
						onUnauthorized()
						return@launch
					}

					uiState = uiState.copy(
						isLoading = false,
						isSubmitting = false,
						error = uploadResult.toMessage()
					)
				}

				ApiOperationResult.NoInternet -> {
					uiState = uiState.copy(isLoading = false, isSubmitting = false)
					onNoInternet {
						createDatingProfile(
							imageBytes = imageBytes,
							onUnauthorized = onUnauthorized,
							onNoInternet = onNoInternet
						)
					}
				}
			}
		}
	}

	private fun ensureDatingProfile(
		onUnauthorized: () -> Unit,
		onNoInternet: (retryAction: () -> Unit) -> Unit
	) {
		viewModelScope.launch {
			uiState = uiState.copy(isLoading = true, error = null, requiresDatingPhoto = false)

			when (val datingProfileResult = getOwnDatingProfileUseCase()) {
				is ApiOperationResult.Success -> {
					if (datingProfileResult.data != null) {
						uiState = uiState.copy(
							isLoading = false,
							requiresDatingPhoto = false,
							message = "Dating profile уже создан"
						)
						return@launch
					}

					uiState = uiState.copy(
						isLoading = false,
						requiresDatingPhoto = true,
						message = null,
						error = null
					)
				}

				is ApiOperationResult.ValidationError -> {
					uiState = uiState.copy(
						isLoading = false,
						error = datingProfileResult.message.orDefaultErrorMessage()
					)
				}

				is ApiOperationResult.ApiError -> {
					if (datingProfileResult.reason == ApiErrorReason.Unauthorized) {
						uiState = uiState.copy(isLoading = false)
						onUnauthorized()
						return@launch
					}

					uiState = uiState.copy(
						isLoading = false,
						error = datingProfileResult.toMessage()
					)
				}

				ApiOperationResult.NoInternet -> {
					uiState = uiState.copy(isLoading = false)
					onNoInternet {
						ensureDatingProfile(
							onUnauthorized = onUnauthorized,
							onNoInternet = onNoInternet
						)
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
				ApiErrorReason.RequestFailed -> "Не удалось выполнить запрос"
			}
		)
	}

	private fun String?.orDefaultErrorMessage(
		fallback: String = "Произошла ошибка"
	): String {
		return this?.takeIf { it.isNotBlank() } ?: fallback
	}
}
