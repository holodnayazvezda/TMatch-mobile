package com.prod.profile.presentation

import com.prod.auth.domain.model.Profile
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prod.common.api.model.ApiErrorReason
import com.prod.common.api.model.ApiOperationResult
import com.prod.profile.domain.model.DatingProfile
import com.prod.profile.domain.model.DatingProfileDraft
import com.prod.profile.domain.usecase.GetOwnAccountProfileUseCase
import com.prod.profile.domain.usecase.GetOwnDatingProfileUseCase
import com.prod.profile.domain.usecase.UpsertOwnDatingProfileUseCase
import com.prod.profile.domain.usecase.UploadDatingPhotoUseCase
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

class ProfileViewModel(
	private val getOwnAccountProfileUseCase: GetOwnAccountProfileUseCase,
	private val getOwnDatingProfileUseCase: GetOwnDatingProfileUseCase,
	private val upsertOwnDatingProfileUseCase: UpsertOwnDatingProfileUseCase,
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
		loadProfileCard(
			onUnauthorized = onUnauthorized,
			onNoInternet = onNoInternet
		)
	}

	fun retry(
		onUnauthorized: () -> Unit,
		onNoInternet: (retryAction: () -> Unit) -> Unit
	) {
		loadProfileCard(
			onUnauthorized = onUnauthorized,
			onNoInternet = onNoInternet
		)
	}

	fun replaceDatingPhoto(
		selectedPhotoIndex: Int,
		imageBytes: ByteArray,
		onUnauthorized: () -> Unit,
		onNoInternet: (retryAction: () -> Unit) -> Unit
	) {
		val currentCard = uiState.profileCard ?: return

		viewModelScope.launch {
			uiState = uiState.copy(isSubmitting = true, error = null)

			when (
				val uploadResult = uploadDatingPhotoUseCase(
					contentType = "image/png",
					bytes = imageBytes
				)
			) {
				is ApiOperationResult.Success -> {
					val currentPhotos = currentCard.datingPhotoUrls
					val updatedPhotos = if (currentPhotos.isEmpty()) {
						listOf(uploadResult.data)
					} else {
						currentPhotos.toMutableList().apply {
							this[selectedPhotoIndex.coerceIn(0, lastIndex)] = uploadResult.data
						}
					}

					when (
						val createResult = upsertOwnDatingProfileUseCase(
							DatingProfileDraft(photos = updatedPhotos)
						)
					) {
						is ApiOperationResult.Success -> {
							uiState = uiState.copy(
								isLoading = false,
								isSubmitting = false,
								profileCard = currentCard.copy(datingPhotoUrls = createResult.data.photos),
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
								replaceDatingPhoto(
									selectedPhotoIndex = selectedPhotoIndex,
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
						replaceDatingPhoto(
							selectedPhotoIndex = selectedPhotoIndex,
							imageBytes = imageBytes,
							onUnauthorized = onUnauthorized,
							onNoInternet = onNoInternet
						)
					}
				}
			}
		}
	}

	private fun loadProfileCard(
		onUnauthorized: () -> Unit,
		onNoInternet: (retryAction: () -> Unit) -> Unit
	) {
		viewModelScope.launch {
			uiState = uiState.copy(isLoading = true, error = null)

			val accountProfile = when (val ownProfileResult = getOwnAccountProfileUseCase()) {
				is ApiOperationResult.Success -> {
					ownProfileResult.data ?: run {
						uiState = uiState.copy(
							isLoading = false,
							error = "Не удалось загрузить данные анкеты"
						)
						return@launch
					}
				}

				is ApiOperationResult.ValidationError -> {
					uiState = uiState.copy(
						isLoading = false,
						error = ownProfileResult.message.orDefaultErrorMessage()
					)
					return@launch
				}

				is ApiOperationResult.ApiError -> {
					if (ownProfileResult.reason == ApiErrorReason.Unauthorized) {
						uiState = uiState.copy(isLoading = false)
						onUnauthorized()
						return@launch
					}

					uiState = uiState.copy(
						isLoading = false,
						error = ownProfileResult.toMessage()
					)
					return@launch
				}

				ApiOperationResult.NoInternet -> {
					uiState = uiState.copy(isLoading = false)
					onNoInternet {
						loadProfileCard(
							onUnauthorized = onUnauthorized,
							onNoInternet = onNoInternet
						)
					}
					return@launch
				}
			}

			when (val datingProfileResult = getOwnDatingProfileUseCase()) {
				is ApiOperationResult.Success -> {
					uiState = uiState.copy(
						isLoading = false,
						profileCard = accountProfile.toProfileCardUiState(datingProfileResult.data),
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
						loadProfileCard(
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

	private fun Profile.toProfileCardUiState(
		datingProfile: DatingProfile?
	): ProfileCardUiState {
		val age = birthDate.toAgeOrNull()
		val fullName = listOfNotNull(
			firstName.takeIf { it.isNotBlank() },
			lastName?.takeIf { it.isNotBlank() }
		).joinToString(" ")

		return ProfileCardUiState(
			firstName = firstName,
			fullName = fullName.ifBlank { firstName },
			age = age,
			birthDateLabel = birthDate.toDisplayDate(),
			region = region?.takeIf { it.isNotBlank() },
			datingPhotoUrls = datingProfile?.photos.orEmpty()
		)
	}

	private fun String.toAgeOrNull(): Int? {
		val birthDate = try {
			LocalDate.parse(this)
		} catch (_: DateTimeParseException) {
			return null
		}

		return Period.between(birthDate, LocalDate.now()).years
	}

	private fun String.toDisplayDate(): String {
		return try {
			LocalDate.parse(this).format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
		} catch (_: DateTimeParseException) {
			this
		}
	}
}
