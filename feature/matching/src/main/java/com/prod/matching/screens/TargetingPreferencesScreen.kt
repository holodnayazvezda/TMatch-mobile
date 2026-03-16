package com.prod.matching.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.prod.match.domain.model.TargetGender
import com.prod.matching.R
import com.prod.matching.presentation.TargetingPreferencesUiState
import com.prod.matching.presentation.TargetingPreferencesViewModel
import com.prod.ui.components.AppFactCard
import com.prod.ui.components.AppScreenTopBar
import com.prod.ui.components.AppSectionCard
import com.prod.ui.components.AppSectionTitle
import com.prod.ui.components.AppTopBarIconAction
import com.prod.ui.components.buttons.AppBigButton
import com.prod.ui.components.buttons.AppMediumButton
import com.prod.ui.components.text_fields.AppTextField
import com.prod.ui.components.text_fields.AppTextFieldWithDropdown
import com.prod.ui.data.RussianRegions
import com.prod.ui.theme.LocalDimensions
import org.koin.androidx.compose.koinViewModel

private const val AgeRangeMin = 18f
private const val AgeRangeMax = 80f

private data class GenderOption(
	val gender: TargetGender,
	val label: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TargetingPreferencesBottomSheet(
	sheetState: SheetState,
	onDismiss: () -> Unit,
	onSaved: () -> Unit,
	onUnauthorized: () -> Unit,
	onRequestNoInternet: (retryAction: () -> Unit) -> Unit,
	modifier: Modifier = Modifier,
	viewModel: TargetingPreferencesViewModel = koinViewModel()
) {
	LaunchedEffect(viewModel, onUnauthorized, onRequestNoInternet) {
		viewModel.initialize(
			onUnauthorized = onUnauthorized,
			onNoInternet = onRequestNoInternet
		)
	}

	val uiState = viewModel.uiState
	val dim = LocalDimensions.current

	ModalBottomSheet(
		onDismissRequest = onDismiss,
		sheetState = sheetState,
		modifier = modifier
	) {
		Column(
			modifier = Modifier
				.fillMaxWidth()
				.padding(
					start = dim.medium,
					top = dim.small,
					end = dim.medium,
					bottom = dim.medium
				),
			verticalArrangement = Arrangement.spacedBy(dim.defaultSpacing)
		) {
			Text(
				text = androidx.compose.ui.res.stringResource(R.string.matching_preferences_title),
				style = MaterialTheme.typography.titleLarge,
				color = MaterialTheme.colorScheme.onSurface
			)
			when {
				uiState.isLoading -> {
					Box(
						modifier = Modifier
							.fillMaxWidth()
							.padding(vertical = dim.extraLarge),
						contentAlignment = Alignment.Center
					) {
						CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
					}
				}

				else -> {
					TargetingPreferencesContent(
						uiState = uiState,
						onGenderSelected = viewModel::onGenderTargetSelected,
						onRegionSelected = viewModel::onRegionSelected,
						onAgeRangeChanged = viewModel::onAgeRangeChanged,
						onRetry = viewModel::retry,
						contentPadding = PaddingValues(0.dp),
						horizontalPadding = 0.dp,
						modifier = Modifier.heightIn(max = 520.dp)
					)
				}
			}
			AppBigButton(
				onClick = {
					viewModel.save(onSaved = onSaved)
				},
				buttonText = androidx.compose.ui.res.stringResource(R.string.matching_preferences_save),
				isLoading = uiState.isSaving
			)
		}
	}
}

@Composable
fun TargetingPreferencesScreen(
	onBack: () -> Unit,
	onSaved: () -> Unit,
	onUnauthorized: () -> Unit,
	onRequestNoInternet: (retryAction: () -> Unit) -> Unit,
	modifier: Modifier = Modifier,
	viewModel: TargetingPreferencesViewModel = koinViewModel()
) {
	LaunchedEffect(viewModel, onUnauthorized, onRequestNoInternet) {
		viewModel.initialize(
			onUnauthorized = onUnauthorized,
			onNoInternet = onRequestNoInternet
		)
	}

	val uiState = viewModel.uiState
	val dim = LocalDimensions.current

	Scaffold(
		modifier = modifier.fillMaxSize(),
		topBar = {
			AppScreenTopBar(
				title = androidx.compose.ui.res.stringResource(R.string.matching_preferences_title),
				navigation = {
					AppTopBarIconAction(
						iconRes = R.drawable.ic_back_24,
						contentDescription = androidx.compose.ui.res.stringResource(R.string.matching_preferences_back),
						onClick = onBack
					)
				}
			)
		},
		bottomBar = {
			Surface(
				color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)
			) {
				AppBigButton(
					modifier = Modifier.padding(
						start = dim.medium,
						top = dim.small,
						end = dim.medium,
						bottom = dim.medium
					),
					onClick = {
						viewModel.save(onSaved = onSaved)
					},
					buttonText = androidx.compose.ui.res.stringResource(R.string.matching_preferences_save),
					isLoading = uiState.isSaving
				)
			}
		},
		containerColor = androidx.compose.ui.graphics.Color.Transparent
	) { innerPadding ->
		when {
			uiState.isLoading -> {
				Column(
					modifier = Modifier
						.fillMaxSize()
						.padding(innerPadding),
					horizontalAlignment = Alignment.CenterHorizontally,
					verticalArrangement = Arrangement.Center
				) {
					CircularProgressIndicator(
						modifier = Modifier.size(40.dp),
						color = MaterialTheme.colorScheme.primary
					)
				}
			}

			else -> {
				TargetingPreferencesContent(
					uiState = uiState,
					onGenderSelected = viewModel::onGenderTargetSelected,
					onRegionSelected = viewModel::onRegionSelected,
					onAgeRangeChanged = viewModel::onAgeRangeChanged,
					onRetry = viewModel::retry,
					contentPadding = innerPadding
				)
			}
		}
	}
}

@Composable
private fun TargetingPreferencesContent(
	uiState: TargetingPreferencesUiState,
	onGenderSelected: (TargetGender) -> Unit,
	onRegionSelected: (String?) -> Unit,
	onAgeRangeChanged: (Int, Int) -> Unit,
	onRetry: () -> Unit,
	contentPadding: PaddingValues,
	horizontalPadding: androidx.compose.ui.unit.Dp = LocalDimensions.current.medium,
	modifier: Modifier = Modifier
) {
	val dim = LocalDimensions.current
	val genderOptions = remember {
		listOf(
			GenderOption(TargetGender.Both, "Все"),
			GenderOption(TargetGender.Female, "Женщины"),
			GenderOption(TargetGender.Male, "Мужчины")
		)
	}
	val regionOptions = remember {
		RussianRegions.all
	}
	var sliderStart by remember(uiState.ageFrom) { mutableFloatStateOf(uiState.ageFrom.toFloat()) }
	var sliderEnd by remember(uiState.ageTo) { mutableFloatStateOf(uiState.ageTo.toFloat()) }

	LaunchedEffect(uiState.ageFrom, uiState.ageTo) {
		sliderStart = uiState.ageFrom.toFloat()
		sliderEnd = uiState.ageTo.toFloat()
	}

	Column(
		modifier = modifier
			.fillMaxSize()
			.padding(contentPadding)
			.verticalScroll(rememberScrollState())
			.padding(
				start = horizontalPadding,
				top = dim.small,
				end = horizontalPadding,
				bottom = dim.extraLarge
			),
		verticalArrangement = Arrangement.spacedBy(dim.defaultSpacing)
		) {
		AppSectionCard {
			Text(
				text = androidx.compose.ui.res.stringResource(R.string.matching_preferences_gender_field_title),
				style = MaterialTheme.typography.titleMedium,
					fontWeight = FontWeight.SemiBold,
					color = MaterialTheme.colorScheme.onSurface
				)
				AppTextFieldWithDropdown(
					value = uiState.genderTarget.label(),
					label = androidx.compose.ui.res.stringResource(R.string.matching_preferences_gender_label),
				dropdownItems = genderOptions,
				onDropdownItemSelected = { option ->
					onGenderSelected(option.gender)
				},
				dropDownItem = { option ->
					Text(text = option.label)
				}
			)
		}

		AppSectionCard {
			Text(
				text = androidx.compose.ui.res.stringResource(R.string.matching_preferences_region_field_title),
				style = MaterialTheme.typography.titleMedium,
				fontWeight = FontWeight.SemiBold,
				color = MaterialTheme.colorScheme.onSurface
			)
			SearchableRegionField(
				selectedRegion = uiState.region,
				regionOptions = regionOptions,
				onRegionSelected = onRegionSelected
			)
		}

		AppSectionCard {
			AppSectionTitle(
				title = androidx.compose.ui.res.stringResource(R.string.matching_preferences_age_title)
			)
			Row(
				modifier = Modifier.fillMaxWidth(),
				horizontalArrangement = Arrangement.spacedBy(dim.small)
			) {
				AppFactCard(
					title = androidx.compose.ui.res.stringResource(R.string.matching_preferences_age_from),
					value = uiState.ageFrom.toString(),
					modifier = Modifier.weight(1f)
				)
				AppFactCard(
					title = androidx.compose.ui.res.stringResource(R.string.matching_preferences_age_to),
					value = uiState.ageTo.toString(),
					modifier = Modifier.weight(1f)
				)
			}
				RangeSlider(
					value = sliderStart..sliderEnd,
					onValueChange = { range ->
						sliderStart = range.start
						sliderEnd = range.endInclusive
						onAgeRangeChanged(
							range.start.toInt(),
							range.endInclusive.toInt()
						)
					},
					onValueChangeFinished = {},
					valueRange = AgeRangeMin..AgeRangeMax,
					steps = (AgeRangeMax - AgeRangeMin - 1).toInt(),
					modifier = Modifier.fillMaxWidth()
			)
		}

		uiState.error?.let { error ->
			AppSectionCard(
				containerColor = MaterialTheme.colorScheme.errorContainer,
				contentColor = MaterialTheme.colorScheme.onErrorContainer
			) {
				Text(
					text = error,
					style = MaterialTheme.typography.labelLarge,
					fontWeight = FontWeight.Medium
				)
				Text(
					text = androidx.compose.ui.res.stringResource(R.string.matching_preferences_retry_hint),
					style = MaterialTheme.typography.labelLarge
				)
				AppMediumButton(
					modifier = Modifier.padding(top = dim.extraSmall),
					onClick = onRetry,
					buttonText = androidx.compose.ui.res.stringResource(R.string.matching_preferences_retry_action),
					isLoading = false
				)
			}
		}
	}
}

@Composable
private fun SearchableRegionField(
	selectedRegion: String?,
	regionOptions: List<String>,
	onRegionSelected: (String?) -> Unit
) {
	val dim = LocalDimensions.current
	var query by remember(selectedRegion) { mutableStateOf(selectedRegion.orEmpty()) }
	var expanded by remember { mutableStateOf(false) }

	LaunchedEffect(selectedRegion) {
		query = selectedRegion.orEmpty()
	}

	val normalizedQuery = query.trim()
	val suggestions = remember(normalizedQuery, regionOptions) {
		if (normalizedQuery.isBlank()) {
			regionOptions.take(6)
		} else {
			regionOptions.filter {
				it.contains(normalizedQuery, ignoreCase = true)
			}.take(6)
		}
	}

	Box(modifier = Modifier.fillMaxWidth()) {
		AppTextField(
			modifier = Modifier.fillMaxWidth(),
			value = query,
			onValueChange = { value ->
				query = value
				expanded = true
				if (value.isBlank()) {
					onRegionSelected(null)
				}
			},
			label = androidx.compose.ui.res.stringResource(R.string.matching_preferences_region_label),
			focusedPlaceholder = androidx.compose.ui.res.stringResource(R.string.matching_preferences_region_search_hint),
			keyboardOptions = KeyboardOptions(
				capitalization = KeyboardCapitalization.Words,
				imeAction = ImeAction.Done
			),
			onDone = {
				val exactMatch = regionOptions.firstOrNull {
					it.equals(query.trim(), ignoreCase = true)
				}
				onRegionSelected(exactMatch)
				expanded = false
			}
		)
		DropdownMenu(
			expanded = expanded && suggestions.isNotEmpty(),
			onDismissRequest = { expanded = false }
		) {
			suggestions.forEach { region ->
				DropdownMenuItem(
					text = { Text(text = region) },
					onClick = {
						query = region
						onRegionSelected(region)
						expanded = false
					}
				)
			}
		}
	}

	if (selectedRegion != null || query.isNotBlank()) {
		AppMediumButton(
			modifier = Modifier.padding(top = dim.extraSmall),
			onClick = {
				query = ""
				expanded = false
				onRegionSelected(null)
			},
			buttonText = androidx.compose.ui.res.stringResource(R.string.matching_preferences_region_reset),
			isLoading = false
		)
	}

}

private fun TargetGender.label(): String {
	return when (this) {
		TargetGender.Male -> "Мужчины"
		TargetGender.Female -> "Женщины"
		TargetGender.Both -> "Все"
	}
}
