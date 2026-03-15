package com.prod.auth.presentation.profile

import android.graphics.Bitmap
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prod.auth.R
import com.prod.auth.domain.model.ProfileDraft
import com.prod.auth.domain.usecase.GetOwnProfileUseCase
import com.prod.auth.domain.usecase.UpsertOwnProfileUseCase
import com.prod.auth.domain.usecase.UploadDatingPhotoUseCase
import com.prod.auth.presentation.common.AuthOperationResultUiMapper
import com.prod.auth.presentation.common.UiText
import com.prod.common.api.model.ApiErrorReason
import com.prod.common.api.model.ApiOperationResult
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

class ProfileSetupViewModel(
	private val getOwnProfileUseCase: GetOwnProfileUseCase,
	private val upsertOwnProfileUseCase: UpsertOwnProfileUseCase,
	private val uploadDatingPhotoUseCase: UploadDatingPhotoUseCase,
	private val authOperationResultUiMapper: AuthOperationResultUiMapper
) : ViewModel() {

	var uiState by mutableStateOf(ProfileSetupUiState())
		private set

	private var isInitialized = false

	fun initialize(
		onProfileReady: () -> Unit,
		onNoInternet: (retryAction: () -> Unit) -> Unit,
		onUnauthorized: () -> Unit
	) {
		if (isInitialized) {
			return
		}
		isInitialized = true
		loadProfileState(
			onProfileReady = onProfileReady,
			onNoInternet = onNoInternet,
			onUnauthorized = onUnauthorized
		)
	}

	fun onFirstNameChange(value: String) {
		uiState = uiState.copy(
			firstName = value,
			firstNameError = null,
			formError = null
		)
	}

	fun onLastNameChange(value: String) {
		uiState = uiState.copy(
			lastName = value,
			formError = null
		)
	}

	fun onRegionChange(value: String) {
		uiState = uiState.copy(
			region = value,
			formError = null
		)
	}

	fun onBirthDateSelected(value: String) {
		val isoDate = value.toIsoLocalDateOrNull() ?: return
		uiState = uiState.copy(
			birthDateIso = isoDate,
			birthDateDisplay = value.toDisplayDate(),
			birthDateError = null,
			formError = null
		)
	}

	fun submit(
		avatarBitmap: Bitmap?,
		onSuccess: () -> Unit,
		onNoInternet: (retryAction: () -> Unit) -> Unit,
		onUnauthorized: () -> Unit
	) {
		val firstName = uiState.firstName.trim()
		val lastName = uiState.lastName.trim().ifBlank { null }
		val birthDate = uiState.birthDateIso
		val region = uiState.region.trim().ifBlank { null }

		val firstNameError = if (firstName.isBlank()) {
			UiText.StringResource(R.string.profile_setup_first_name_required)
		} else {
			null
		}
		val birthDateError = if (birthDate.isNullOrBlank()) {
			UiText.StringResource(R.string.profile_setup_birth_date_required)
		} else {
			null
		}

		uiState = uiState.copy(
			firstNameError = firstNameError,
			birthDateError = birthDateError,
			formError = null
		)

		if (firstNameError != null || birthDateError != null || birthDate == null) {
			return
		}

		viewModelScope.launch {
			uiState = uiState.copy(isSubmitting = true)

			val avatarUrl = when {
				avatarBitmap == null -> null
				else -> when (
					val avatarUploadResult = uploadDatingPhotoUseCase(
						contentType = "image/png",
						bytes = avatarBitmap.toPngByteArray()
					)
				) {
					is ApiOperationResult.Success -> avatarUploadResult.data

					is ApiOperationResult.ValidationError -> {
						uiState = uiState.copy(
							isSubmitting = false,
							formError = authOperationResultUiMapper.mapValidationError(avatarUploadResult)
						)
						return@launch
					}

					is ApiOperationResult.ApiError -> {
						if (avatarUploadResult.reason == ApiErrorReason.Unauthorized) {
							uiState = uiState.copy(isSubmitting = false)
							onUnauthorized()
							return@launch
						}

						uiState = uiState.copy(
							isSubmitting = false,
							formError = authOperationResultUiMapper.mapApiError(avatarUploadResult)
						)
						return@launch
					}

					ApiOperationResult.NoInternet -> {
						uiState = uiState.copy(isSubmitting = false)
						onNoInternet {
							submit(
								avatarBitmap = avatarBitmap,
								onSuccess = onSuccess,
								onNoInternet = onNoInternet,
								onUnauthorized = onUnauthorized
							)
						}
						return@launch
					}
				}
			}

			when (
				val createProfileResult = upsertOwnProfileUseCase(
					ProfileDraft(
						firstName = firstName,
						lastName = lastName,
						birthDate = birthDate,
						region = region,
						avatarUrl = avatarUrl
					)
				)
			) {
				is ApiOperationResult.Success -> {
					uiState = uiState.copy(isSubmitting = false)
					onSuccess()
				}

				is ApiOperationResult.ValidationError -> {
					uiState = uiState.copy(
						isSubmitting = false,
						formError = authOperationResultUiMapper.mapValidationError(createProfileResult)
					)
				}

				is ApiOperationResult.ApiError -> {
					if (createProfileResult.reason == ApiErrorReason.Unauthorized) {
						uiState = uiState.copy(isSubmitting = false)
						onUnauthorized()
						return@launch
					}

					uiState = uiState.copy(
						isSubmitting = false,
						formError = authOperationResultUiMapper.mapApiError(createProfileResult)
					)
				}

				ApiOperationResult.NoInternet -> {
					uiState = uiState.copy(isSubmitting = false)
					onNoInternet {
						submit(
							avatarBitmap = avatarBitmap,
							onSuccess = onSuccess,
							onNoInternet = onNoInternet,
							onUnauthorized = onUnauthorized
						)
					}
				}
			}
		}
	}

	private fun loadProfileState(
		onProfileReady: () -> Unit,
		onNoInternet: (retryAction: () -> Unit) -> Unit,
		onUnauthorized: () -> Unit
	) {
		viewModelScope.launch {
			uiState = uiState.copy(isCheckingProfile = true, formError = null)
			when (val result = getOwnProfileUseCase()) {
				is ApiOperationResult.Success -> {
					uiState = uiState.copy(isCheckingProfile = false)
					if (result.data != null) {
						onProfileReady()
					}
				}

				is ApiOperationResult.ValidationError -> {
					uiState = uiState.copy(
						isCheckingProfile = false,
						formError = authOperationResultUiMapper.mapValidationError(result)
					)
				}

				is ApiOperationResult.ApiError -> {
					if (result.reason == ApiErrorReason.Unauthorized) {
						uiState = uiState.copy(isCheckingProfile = false)
						onUnauthorized()
						return@launch
					}

					uiState = uiState.copy(
						isCheckingProfile = false,
						formError = authOperationResultUiMapper.mapApiError(result)
					)
				}

				ApiOperationResult.NoInternet -> {
					uiState = uiState.copy(isCheckingProfile = false)
					onNoInternet {
						loadProfileState(
							onProfileReady = onProfileReady,
							onNoInternet = onNoInternet,
							onUnauthorized = onUnauthorized
						)
					}
				}
			}
		}
	}

	private fun String.toIsoLocalDateOrNull(): String? {
		val formatter = DateTimeFormatter.ofPattern("ddMMyyyy")
		val localDate = runCatching { LocalDate.parse(this, formatter) }.getOrNull() ?: return null
		return localDate.toString()
	}

	private fun String.toDisplayDate(): String {
		val formatter = DateTimeFormatter.ofPattern("ddMMyyyy")
		val parsed = try {
			LocalDate.parse(this, formatter)
		} catch (_: DateTimeParseException) {
			return this
		}
		return parsed.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
	}

	private fun Bitmap.toPngByteArray(): ByteArray {
		val stream = ByteArrayOutputStream()
		compress(Bitmap.CompressFormat.PNG, 100, stream)
		return stream.toByteArray()
	}
}
