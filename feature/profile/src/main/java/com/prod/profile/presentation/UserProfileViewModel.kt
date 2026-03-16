package com.prod.profile.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prod.common.api.model.ApiErrorReason
import com.prod.common.api.model.ApiOperationResult
import com.prod.profile.domain.model.UserCard
import com.prod.profile.domain.usecase.GetUserCardUseCase
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

class UserProfileViewModel(
	private val getUserCardUseCase: GetUserCardUseCase
) : ViewModel() {

	var uiState by mutableStateOf(UserProfileUiState())
		private set

	private var loadedUserId: String? = null

	fun initialize(
		userId: String,
		onUnauthorized: () -> Unit,
		onNoInternet: (retryAction: () -> Unit) -> Unit
	) {
		if (loadedUserId == userId && (uiState.profileCard != null || uiState.isLoading)) {
			return
		}

		loadedUserId = userId
		loadUserCard(
			userId = userId,
			onUnauthorized = onUnauthorized,
			onNoInternet = onNoInternet
		)
	}

	fun retry(
		onUnauthorized: () -> Unit,
		onNoInternet: (retryAction: () -> Unit) -> Unit
	) {
		val userId = loadedUserId ?: return
		loadUserCard(
			userId = userId,
			onUnauthorized = onUnauthorized,
			onNoInternet = onNoInternet
		)
	}

	private fun loadUserCard(
		userId: String,
		onUnauthorized: () -> Unit,
		onNoInternet: (retryAction: () -> Unit) -> Unit
	) {
		viewModelScope.launch {
			uiState = uiState.copy(isLoading = true, error = null)
			when (val result = getUserCardUseCase(userId)) {
				is ApiOperationResult.Success -> {
					uiState = uiState.copy(
						isLoading = false,
						profileCard = result.data.toUiState(),
						error = null
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
						onUnauthorized()
						return@launch
					}

					uiState = uiState.copy(
						isLoading = false,
						error = result.toMessage()
					)
				}

				ApiOperationResult.NoInternet -> {
					uiState = uiState.copy(isLoading = false, error = null)
					onNoInternet {
						loadUserCard(
							userId = userId,
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
				ApiErrorReason.RequestFailed -> "Не удалось загрузить профиль"
			}
		)
	}

	private fun String?.orDefaultErrorMessage(
		fallback: String = "Не удалось загрузить профиль"
	): String {
		return this?.takeIf { it.isNotBlank() } ?: fallback
	}
}

private fun UserCard.toUiState(): UserProfileCardUiState {
	val profile = profile
	val birthDate = profile?.birthDate
	val firstName = profile?.firstName.orEmpty()
	val fullName = listOfNotNull(
		profile?.firstName?.takeIf { it.isNotBlank() },
		profile?.lastName?.takeIf { it.isNotBlank() }
	).joinToString(" ").ifBlank {
		firstName.ifBlank { "Пользователь" }
	}

	return UserProfileCardUiState(
		userId = userId,
		fullName = fullName,
		age = birthDate?.toAgeOrNull(),
		birthDateLabel = birthDate?.toDisplayDate() ?: "Не указана",
		region = profile?.region?.takeIf { it.isNotBlank() },
		avatarUrl = profile?.avatarUrl,
		datingPhotoUrls = datingProfile?.photos.orEmpty(),
		visibleTraitLabels = datingProfile
			?.traits
			.orEmpty()
			.filterNot { it.isHidden }
			.sortedByDescending { it.score }
			.map { it.traitCode.toDisplayTraitLabel() }
			.distinct()
	)
}

private fun String.toAgeOrNull(): Int? {
	val birthDate = runCatching { LocalDate.parse(this) }.getOrNull() ?: return null
	return Period.between(birthDate, LocalDate.now()).years.takeIf { it >= 0 }
}

private fun String.toDisplayDate(): String {
	return try {
		LocalDate.parse(this).format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
	} catch (_: DateTimeParseException) {
		this
	}
}

private fun String.toDisplayTraitLabel(): String {
	return split('_', '-', ' ')
		.filter { it.isNotBlank() }
		.joinToString(" ") { part ->
			part.lowercase().replaceFirstChar { it.titlecase() }
		}
		.ifBlank { this }
}
