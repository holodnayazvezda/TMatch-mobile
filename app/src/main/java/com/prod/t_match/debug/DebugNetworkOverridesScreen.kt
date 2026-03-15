package com.prod.t_match.debug

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.prod.tmatch_api.common.data.network.ApiResponseOverride
import com.prod.tmatch_api.common.data.network.ApiResponseOverrideRegistry
import com.prod.ui.components.buttons.AppBigButton
import com.prod.ui.components.buttons.AppMediumButton
import com.prod.ui.components.text_fields.AppTextFieldWithDropdown
import io.ktor.http.HttpMethod
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import org.koin.compose.koinInject

@Composable
fun DebugNetworkOverridesScreen(
	onBack: () -> Unit,
	modifier: Modifier = Modifier,
	registry: ApiResponseOverrideRegistry = koinInject()
) {
	val endpointDefinitions = remember { debugEndpointDefinitions }
	val itemStateHolders = remember(registry) {
		mutableStateListOf<EndpointOverrideItemStateHolder>().apply {
			addAll(
				endpointDefinitions.map { definition ->
					val override = registry.getOverride(definition.method, definition.path)
					EndpointOverrideItemStateHolder(
						initialState = EndpointOverrideEditorState(
							definition = definition,
							enabled = override != null,
							statusCode = override?.statusCode?.toString() ?: definition.defaultStatusCode.toString(),
							savedStatusCode = override?.statusCode?.toString(),
							body = (override?.body ?: definition.defaultBody).toPrettyJson(),
							savedBody = override?.body?.toPrettyJson(),
							selectedPresetLabel = null,
							error = null
						)
					)
				}
			)
		}
	}
	var selectedKey by remember { mutableStateOf<String?>(null) }
	val selectedHolder = itemStateHolders.firstOrNull { it.key == selectedKey }

	BackHandler(enabled = selectedHolder != null) {
		selectedKey = null
	}

	if (selectedHolder == null) {
		DebugOverridesListScreen(
			modifier = modifier,
			itemStateHolders = itemStateHolders,
			onBack = onBack,
			onClearAll = {
				registry.clear()
				itemStateHolders.forEachIndexed { index, holder ->
					val definition = endpointDefinitions[index]
					holder.state = EndpointOverrideEditorState(
						definition = definition,
						enabled = false,
						statusCode = definition.defaultStatusCode.toString(),
						savedStatusCode = null,
						body = definition.defaultBody.toPrettyJson(),
						savedBody = null,
						selectedPresetLabel = null,
						error = null
					)
				}
				selectedKey = null
			},
			onToggleEnabled = { holder, isEnabled ->
				val state = holder.state
				holder.state = state.copy(
					enabled = isEnabled,
					statusCode = if (isEnabled) state.savedStatusCode ?: state.statusCode else state.statusCode,
					body = if (isEnabled) state.savedBody ?: state.body else state.body,
					selectedPresetLabel = null,
					error = null
				)
			},
			onOpenConfig = { holder ->
				selectedKey = holder.key
			}
		)
	} else {
		DebugOverrideEditorScreen(
			modifier = modifier,
			state = selectedHolder.state,
			onBack = { selectedKey = null },
			onStateChange = { updatedState ->
				selectedHolder.state = updatedState
			},
			onApply = {
				val state = selectedHolder.state
				val statusCode = state.statusCode.toIntOrNull()
				if (statusCode == null || statusCode !in 100..599) {
					selectedHolder.state = state.copy(error = "Укажи HTTP status в диапазоне 100..599")
					return@DebugOverrideEditorScreen
				}

				if (state.enabled) {
					registry.setOverride(
						method = state.definition.method,
						path = state.definition.path,
						response = ApiResponseOverride(
							statusCode = statusCode,
							body = state.body
						)
					)
					selectedHolder.state = state.copy(
						savedStatusCode = statusCode.toString(),
						savedBody = state.body,
						error = null
					)
				} else {
					registry.removeOverride(
						method = state.definition.method,
						path = state.definition.path
					)
					selectedHolder.state = state.copy(
						savedStatusCode = null,
						savedBody = null,
						error = null
					)
				}
			}
		)
	}
}

@Composable
private fun DebugOverridesListScreen(
	modifier: Modifier,
	itemStateHolders: List<EndpointOverrideItemStateHolder>,
	onBack: () -> Unit,
	onClearAll: () -> Unit,
	onToggleEnabled: (EndpointOverrideItemStateHolder, Boolean) -> Unit,
	onOpenConfig: (EndpointOverrideItemStateHolder) -> Unit
) {
	LazyColumn(
		modifier = modifier.fillMaxSize(),
		contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
		verticalArrangement = Arrangement.spacedBy(16.dp)
	) {
		item(key = "header") {
			Surface(
				modifier = Modifier.fillMaxWidth(),
				color = MaterialTheme.colorScheme.surfaceContainerHigh,
				shape = MaterialTheme.shapes.large
			) {
				Column(
					modifier = Modifier.padding(16.dp),
					verticalArrangement = Arrangement.spacedBy(8.dp)
				) {
					Text(
						text = "Debug API Overrides",
						style = MaterialTheme.typography.headlineMedium,
						fontWeight = FontWeight.SemiBold
					)
					Text(
						text = "Список ручек и быстрые переключатели. Детальная настройка открывается отдельно для одной ручки.",
						style = MaterialTheme.typography.bodyMedium,
						color = MaterialTheme.colorScheme.onSurfaceVariant
					)
				}
			}
		}

		item(key = "actions") {
			Row(
				modifier = Modifier.fillMaxWidth(),
				horizontalArrangement = Arrangement.spacedBy(12.dp)
			) {
				AppMediumButton(
					modifier = Modifier.weight(1f),
					onClick = onBack,
					buttonText = "Назад",
					isLoading = false
				)
				AppBigButton(
					modifier = Modifier.weight(1f),
					onClick = onClearAll,
					buttonText = "Очистить все",
					isLoading = false
				)
			}
		}

		items(
			items = itemStateHolders,
			key = { it.key }
		) { holder ->
			DebugOverrideListItem(
				state = holder.state,
				onToggleEnabled = { isEnabled -> onToggleEnabled(holder, isEnabled) },
				onOpenConfig = { onOpenConfig(holder) }
			)
		}
	}
}

@Composable
private fun DebugOverrideListItem(
	state: EndpointOverrideEditorState,
	onToggleEnabled: (Boolean) -> Unit,
	onOpenConfig: () -> Unit
) {
	val summary = if (state.enabled) {
		"HTTP ${state.statusCode}${state.selectedPresetLabel?.let { " • $it" } ?: ""}"
	} else {
		"Override выключен"
	}

	Card(
		modifier = Modifier.fillMaxWidth()
	) {
		Column(
			modifier = Modifier.padding(16.dp),
			verticalArrangement = Arrangement.spacedBy(12.dp)
		) {
			Row(
				modifier = Modifier.fillMaxWidth(),
				verticalAlignment = Alignment.CenterVertically,
				horizontalArrangement = Arrangement.SpaceBetween
			) {
				Column(
					modifier = Modifier.weight(1f),
					verticalArrangement = Arrangement.spacedBy(4.dp)
				) {
					Text(
						text = state.definition.title,
						style = MaterialTheme.typography.titleMedium,
						fontWeight = FontWeight.SemiBold
					)
					Text(
						text = "${state.definition.method.value} ${state.definition.path}",
						style = MaterialTheme.typography.bodySmall,
						color = MaterialTheme.colorScheme.onSurfaceVariant
					)
				}
				Switch(
					checked = state.enabled,
					onCheckedChange = onToggleEnabled
				)
			}

			Text(
				text = summary,
				style = MaterialTheme.typography.bodyMedium,
				color = MaterialTheme.colorScheme.onSurfaceVariant
			)

			AppMediumButton(
				onClick = onOpenConfig,
				buttonText = "Настроить",
				isLoading = false
			)
		}
	}
}

@Composable
private fun DebugOverrideEditorScreen(
	modifier: Modifier,
	state: EndpointOverrideEditorState,
	onBack: () -> Unit,
	onStateChange: (EndpointOverrideEditorState) -> Unit,
	onApply: () -> Unit
) {
	val hasUnsavedChanges = state.enabled && (
		state.statusCode != state.savedStatusCode ||
			state.body != state.savedBody
	)

	LazyColumn(
		modifier = modifier.fillMaxSize(),
		contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
		verticalArrangement = Arrangement.spacedBy(16.dp)
	) {
		item(key = "header") {
			Surface(
				modifier = Modifier.fillMaxWidth(),
				color = MaterialTheme.colorScheme.surfaceContainerHigh,
				shape = MaterialTheme.shapes.large
			) {
				Column(
					modifier = Modifier.padding(16.dp),
					verticalArrangement = Arrangement.spacedBy(8.dp)
				) {
					Text(
						text = state.definition.title,
						style = MaterialTheme.typography.headlineMedium,
						fontWeight = FontWeight.SemiBold
					)
					Text(
						text = "${state.definition.method.value} ${state.definition.path}",
						style = MaterialTheme.typography.bodyMedium,
						color = MaterialTheme.colorScheme.onSurfaceVariant
					)
				}
			}
		}

		item(key = "actions") {
			Row(
				modifier = Modifier.fillMaxWidth(),
				horizontalArrangement = Arrangement.spacedBy(12.dp)
			) {
				AppMediumButton(
					modifier = Modifier.weight(1f),
					onClick = onBack,
					buttonText = "К списку",
					isLoading = false
				)
				AppBigButton(
					modifier = Modifier.weight(1f),
					onClick = onApply,
					buttonText = if (hasUnsavedChanges) "Сохранить" else "Пересохранить",
					isLoading = false
				)
			}
		}

		item(key = "enabled") {
			Card(modifier = Modifier.fillMaxWidth()) {
				Row(
					modifier = Modifier
						.fillMaxWidth()
						.padding(16.dp),
					verticalAlignment = Alignment.CenterVertically,
					horizontalArrangement = Arrangement.SpaceBetween
				) {
					Column(
						modifier = Modifier.weight(1f),
						verticalArrangement = Arrangement.spacedBy(4.dp)
					) {
						Text("Включить override", style = MaterialTheme.typography.titleMedium)
						Text(
							text = if (state.enabled) "Сейчас будет использоваться mock" else "Сейчас запрос идёт в backend",
							style = MaterialTheme.typography.bodySmall,
							color = MaterialTheme.colorScheme.onSurfaceVariant
						)
					}
					Switch(
						checked = state.enabled,
						onCheckedChange = { isChecked ->
							onStateChange(
								state.copy(
									enabled = isChecked,
									statusCode = if (isChecked) state.savedStatusCode ?: state.statusCode else state.statusCode,
									body = if (isChecked) state.savedBody ?: state.body else state.body,
									selectedPresetLabel = null,
									error = null
								)
							)
						}
					)
				}
			}
		}

		item(key = "status") {
			DebugDropdownSelector(
				title = "HTTP status",
				value = state.statusCode,
				options = debugStatusOptions,
				optionLabel = { option -> "${option.code} ${option.label}" },
				onOptionSelected = { option ->
					onStateChange(
						state.copy(
							statusCode = option.code.toString(),
							body = if (option.code < 400) {
								state.definition.defaultBody.toPrettyJson()
							} else {
								state.body
							},
							selectedPresetLabel = null,
							error = null
						)
					)
				}
			)
		}

		item(key = "preset") {
			DebugDropdownSelector(
				title = "Error preset",
				value = state.selectedPresetLabel ?: "Выбери error preset",
				options = debugErrorPresets,
				optionLabel = { preset -> "${preset.statusCode} ${preset.label}" },
				onOptionSelected = { preset ->
					onStateChange(
						state.copy(
							statusCode = preset.statusCode.toString(),
							body = preset.body.toPrettyJson(),
							selectedPresetLabel = preset.label,
							error = null
						)
					)
				}
			)
		}

		item(key = "body") {
			OutlinedTextField(
				value = state.body,
				onValueChange = { value ->
					onStateChange(
						state.copy(
							body = value,
							selectedPresetLabel = null,
							error = null
						)
					)
				},
				modifier = Modifier.fillMaxWidth(),
				label = { Text("Response body") },
				minLines = 10,
				keyboardOptions = KeyboardOptions(
					keyboardType = KeyboardType.Text,
					imeAction = ImeAction.Default,
					capitalization = KeyboardCapitalization.None,
					autoCorrectEnabled = false
				)
			)
		}

		item(key = "format") {
			AppMediumButton(
				modifier = Modifier.fillMaxWidth(),
				onClick = {
					onStateChange(
						state.copy(
							body = state.body.toPrettyJson(),
							error = null
						)
					)
				},
				buttonText = "Форматировать JSON",
				isLoading = false
			)
		}

		state.error?.let { error ->
			item(key = "error") {
				Text(
					text = error,
					style = MaterialTheme.typography.bodySmall,
					color = MaterialTheme.colorScheme.error
				)
			}
		}
	}
}

@Composable
private fun <T> DebugDropdownSelector(
	title: String,
	value: String,
	options: List<T>,
	optionLabel: (T) -> String,
	onOptionSelected: (T) -> Unit
) {
	Column(
		verticalArrangement = Arrangement.spacedBy(8.dp)
	) {
		AppTextFieldWithDropdown(
			modifier = Modifier.fillMaxWidth(),
			value = value,
			label = title,
			dropdownItems = options,
			onDropdownItemSelected = onOptionSelected,
			dropDownItem = { option ->
				Text(optionLabel(option))
			}
		)
	}
}

@Stable
private class EndpointOverrideItemStateHolder(
	initialState: EndpointOverrideEditorState
) {
	val key: String = initialState.definition.key
	var state by mutableStateOf(initialState)
}

@Immutable
private data class EndpointOverrideEditorState(
	val definition: DebugEndpointDefinition,
	val enabled: Boolean,
	val statusCode: String,
	val savedStatusCode: String?,
	val body: String,
	val savedBody: String?,
	val selectedPresetLabel: String?,
	val error: String?
)

@Immutable
private data class DebugEndpointDefinition(
	val title: String,
	val method: HttpMethod,
	val path: String,
	val defaultStatusCode: Int,
	val defaultBody: String
) {
	val key: String
		get() = "${method.value}:$path"
}

@Immutable
private data class DebugStatusOption(
	val code: Int,
	val label: String
)

@Immutable
private data class DebugErrorPreset(
	val statusCode: Int,
	val label: String,
	val body: String
)

private val debugStatusOptions = listOf(
	DebugStatusOption(200, "OK"),
	DebugStatusOption(201, "Created"),
	DebugStatusOption(204, "No Content"),
	DebugStatusOption(400, "Bad Request"),
	DebugStatusOption(401, "Unauthorized"),
	DebugStatusOption(404, "Not Found"),
	DebugStatusOption(409, "Conflict"),
	DebugStatusOption(422, "Validation Error"),
	DebugStatusOption(500, "Internal Server Error"),
	DebugStatusOption(503, "Service Unavailable")
)

private val debugErrorPresets = listOf(
	DebugErrorPreset(
		statusCode = 401,
		label = "Unauthorized",
		body = """{"detail":"Unauthorized"}"""
	),
	DebugErrorPreset(
		statusCode = 422,
		label = "Validation Error",
		body = """{"detail":"Validation failed"}"""
	),
	DebugErrorPreset(
		statusCode = 503,
		label = "Service Unavailable",
		body = """{"detail":"Service temporarily unavailable"}"""
	)
)

private const val DefaultMockImageUrl = "https://images.unsplash.com/photo-1539571696357-5a69c17a67c6"

@OptIn(ExperimentalSerializationApi::class)
private val debugJsonFormatter = Json {
	prettyPrint = true
	prettyPrintIndent = "  "
}

private val debugEndpointDefinitions = listOf(
	DebugEndpointDefinition(
		title = "Sign In",
		method = HttpMethod.Post,
		path = "/auth/signing/email",
		defaultStatusCode = 201,
		defaultBody = """{"access_token":"debug-token","user_id":"debug-user"}"""
	),
	DebugEndpointDefinition(
		title = "Sign Up",
		method = HttpMethod.Post,
		path = "/auth/registration/email",
		defaultStatusCode = 201,
		defaultBody = """{"access_token":"debug-token","user_id":"debug-user"}"""
	),
	DebugEndpointDefinition(
		title = "Get Own Profile",
		method = HttpMethod.Get,
		path = "/profile",
		defaultStatusCode = 200,
		defaultBody = """{"user_id":"debug-user","first_name":"Max","last_name":"Ivanov","birth_date":"1995-01-01","gender":"male","region":"Moscow","avatar_url":"$DefaultMockImageUrl"}"""
	),
	DebugEndpointDefinition(
		title = "Upsert Own Profile",
		method = HttpMethod.Put,
		path = "/profile",
		defaultStatusCode = 200,
		defaultBody = """{"user_id":"debug-user","first_name":"Max","last_name":"Ivanov","birth_date":"1995-01-01","gender":"male","region":"Moscow","avatar_url":"$DefaultMockImageUrl"}"""
	),
	DebugEndpointDefinition(
		title = "Get Dating Profile",
		method = HttpMethod.Get,
		path = "/dating-profile/me",
		defaultStatusCode = 200,
		defaultBody = """{"user_id":"debug-user","photos":["$DefaultMockImageUrl","$DefaultMockImageUrl"],"traits":[]}"""
	),
	DebugEndpointDefinition(
		title = "Upsert Dating Profile",
		method = HttpMethod.Put,
		path = "/dating-profile/me",
		defaultStatusCode = 200,
		defaultBody = """{"user_id":"debug-user","photos":["$DefaultMockImageUrl","$DefaultMockImageUrl"],"traits":[]}"""
	),
	DebugEndpointDefinition(
		title = "Upload Dating Photo",
		method = HttpMethod.Post,
		path = "/dating-profile/me/photos",
		defaultStatusCode = 201,
		defaultBody = """{"photo_url":"$DefaultMockImageUrl"}"""
	),
	DebugEndpointDefinition(
		title = "Get Recommendations",
		method = HttpMethod.Get,
		path = "/recommendations",
		defaultStatusCode = 200,
		defaultBody = """{"items":[{"ml_recommendation_id":"rec-1","user_id":"debug-user","candidate_user_id":"candidate-1","reasons":{"ACTIVITY_FREQUENCY":0.98,"RESTAURANTS":0.74,"EVENING_ACTIVITY":0.61},"candidate_card":{"user_id":"candidate-1","profile":{"first_name":"Anna","last_name":"Smirnova","birth_date":"1997-04-03","region":"Saint Petersburg","avatar_url":"$DefaultMockImageUrl"},"dating_profile":{"photos":["$DefaultMockImageUrl"],"traits":[]}}}]}"""
	),
	DebugEndpointDefinition(
		title = "Create Interaction",
		method = HttpMethod.Post,
		path = "/interactions",
		defaultStatusCode = 201,
		defaultBody = """{"interaction_id":"interaction-1","actor_user_id":"debug-user","candidate_user_id":"candidate-1","action":"like","created_at":"2026-03-15T12:00:00Z","ml_recommendation_id":"rec-1"}"""
	)
)

private fun String.toPrettyJson(): String {
	return runCatching {
		debugJsonFormatter.encodeToString(
			kotlinx.serialization.json.JsonElement.serializer(),
			debugJsonFormatter.parseToJsonElement(this)
		)
	}.getOrDefault(this)
}
