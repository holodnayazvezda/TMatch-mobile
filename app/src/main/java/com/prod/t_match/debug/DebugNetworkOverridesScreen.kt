package com.prod.t_match.debug

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
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
import org.koin.compose.koinInject

@Composable
fun DebugNetworkOverridesScreen(
	onBack: () -> Unit,
	modifier: Modifier = Modifier,
	registry: ApiResponseOverrideRegistry = koinInject()
) {
	val editorStates = remember(registry) {
		mutableStateListOf<EndpointOverrideEditorState>().apply {
			addAll(
				debugEndpointDefinitions().map { definition ->
					val override = registry.getOverride(definition.method, definition.path)
						EndpointOverrideEditorState(
							definition = definition,
							enabled = override != null,
							statusCode = override?.statusCode?.toString() ?: definition.defaultStatusCode.toString(),
							savedStatusCode = override?.statusCode?.toString(),
							body = override?.body ?: definition.defaultBody,
							savedBody = override?.body,
							selectedPresetLabel = null,
							error = null
						)
				}
			)
		}
	}

	Column(
		modifier = modifier
			.fillMaxSize()
			.safeDrawingPadding()
			.verticalScroll(rememberScrollState())
			.padding(horizontal = 20.dp, vertical = 16.dp),
		verticalArrangement = Arrangement.spacedBy(16.dp)
	) {
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
					fontWeight = FontWeight.SemiBold,
					color = MaterialTheme.colorScheme.onSurface
				)
				Text(
					text = "Для каждой ручки можно включить override, задать HTTP-статус и тело ответа. Если override выключен, запрос идет в реальный backend.",
					style = MaterialTheme.typography.bodyMedium,
					color = MaterialTheme.colorScheme.onSurfaceVariant
				)
			}
		}

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
				onClick = {
					registry.clear()
					val refreshedStates = debugEndpointDefinitions().map { definition ->
							EndpointOverrideEditorState(
								definition = definition,
								enabled = false,
								statusCode = definition.defaultStatusCode.toString(),
								savedStatusCode = null,
								body = definition.defaultBody,
								savedBody = null,
								selectedPresetLabel = null,
								error = null
							)
						}
					editorStates.clear()
					editorStates.addAll(refreshedStates)
				},
				buttonText = "Очистить все",
				isLoading = false
			)
		}

		editorStates.forEachIndexed { index, state ->
			DebugEndpointOverrideCard(
				state = state,
				onStateChange = { updatedState ->
					editorStates[index] = updatedState
				},
				onApply = {
					val statusCode = state.statusCode.toIntOrNull()
					if (statusCode == null || statusCode !in 100..599) {
						editorStates[index] = state.copy(error = "Укажи HTTP status в диапазоне 100..599")
						return@DebugEndpointOverrideCard
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
						editorStates[index] = state.copy(
							savedStatusCode = statusCode.toString(),
							savedBody = state.body,
							error = null
						)
					} else {
						registry.removeOverride(
							method = state.definition.method,
							path = state.definition.path
						)
						editorStates[index] = state.copy(
							savedStatusCode = null,
							savedBody = null,
							error = null
						)
					}
				}
			)
		}
	}
}

@Composable
private fun DebugEndpointOverrideCard(
	state: EndpointOverrideEditorState,
	onStateChange: (EndpointOverrideEditorState) -> Unit,
	onApply: () -> Unit
) {
	val hasUnsavedChanges = state.enabled && (
		state.statusCode != state.savedStatusCode ||
			state.body != state.savedBody
	)

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

			if (state.enabled) {
				Text(
					text = if (hasUnsavedChanges) {
						"Есть несохраненные изменения"
					} else {
						"Override сохранен"
					},
					style = MaterialTheme.typography.bodySmall,
					color = if (hasUnsavedChanges) {
						MaterialTheme.colorScheme.tertiary
					} else {
						MaterialTheme.colorScheme.onSurfaceVariant
					}
				)
			}

			AppTextFieldWithDropdown(
				value = state.statusCode,
				label = "HTTP status",
				readOnly = true,
				dropdownItems = debugStatusOptions,
				onDropdownItemSelected = { option ->
						onStateChange(
							state.copy(
								statusCode = option.code.toString(),
								selectedPresetLabel = null,
								error = null
							)
						)
				},
				dropDownItem = { option ->
					Text("${option.code} ${option.label}")
				}
			)

			AppTextFieldWithDropdown(
				value = state.selectedPresetLabel ?: "Выбери error preset",
				label = "Error preset",
				readOnly = true,
				dropdownItems = debugErrorPresets,
				onDropdownItemSelected = { preset ->
					onStateChange(
						state.copy(
							statusCode = preset.statusCode.toString(),
							body = preset.body,
							selectedPresetLabel = preset.label,
							error = null
						)
					)
				},
				dropDownItem = { preset ->
					Text("${preset.statusCode} ${preset.label}")
				}
			)

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
				minLines = 6,
				keyboardOptions = KeyboardOptions(
					keyboardType = KeyboardType.Text,
					imeAction = ImeAction.Default,
					capitalization = KeyboardCapitalization.None,
					autoCorrectEnabled = false
				)
			)

			state.error?.let { error ->
				Text(
					text = error,
					style = MaterialTheme.typography.bodySmall,
					color = MaterialTheme.colorScheme.error
				)
			}

			if (state.enabled) {
				AppBigButton(
					onClick = onApply,
					buttonText = if (hasUnsavedChanges) "Сохранить изменения" else "Пересохранить override",
					isLoading = false
				)
			}
		}
	}
}

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

private data class DebugEndpointDefinition(
	val title: String,
	val method: HttpMethod,
	val path: String,
	val defaultStatusCode: Int,
	val defaultBody: String
)

private data class DebugStatusOption(
	val code: Int,
	val label: String
)

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

private fun debugEndpointDefinitions(): List<DebugEndpointDefinition> {
	return listOf(
		DebugEndpointDefinition(
			title = "Sign In",
			method = HttpMethod.Post,
			path = "/auth/signing/email",
			defaultStatusCode = 200,
			defaultBody = """{"access_token":"debug-token","user_id":"debug-user"}"""
		),
		DebugEndpointDefinition(
			title = "Sign Up",
			method = HttpMethod.Post,
			path = "/auth/registration/email",
			defaultStatusCode = 200,
			defaultBody = """{"access_token":"debug-token","user_id":"debug-user"}"""
		),
		DebugEndpointDefinition(
			title = "Get Own Profile",
			method = HttpMethod.Get,
			path = "/profile",
			defaultStatusCode = 200,
			defaultBody = """{"user_id":"debug-user","first_name":"Max","last_name":"Ivanov","birth_date":"1995-01-01","region":"Moscow","avatar_url":"$DefaultMockImageUrl"}"""
		),
		DebugEndpointDefinition(
			title = "Upsert Own Profile",
			method = HttpMethod.Put,
			path = "/profile",
			defaultStatusCode = 200,
			defaultBody = """{"user_id":"debug-user","first_name":"Max","last_name":"Ivanov","birth_date":"1995-01-01","region":"Moscow","avatar_url":"$DefaultMockImageUrl"}"""
		),
		DebugEndpointDefinition(
			title = "Get Dating Profile",
			method = HttpMethod.Get,
			path = "/dating-profile/me",
			defaultStatusCode = 200,
			defaultBody = """{"user_id":"debug-user","photos":["$DefaultMockImageUrl","$DefaultMockImageUrl"]}"""
		),
		DebugEndpointDefinition(
			title = "Upsert Dating Profile",
			method = HttpMethod.Put,
			path = "/dating-profile/me",
			defaultStatusCode = 200,
			defaultBody = """{"user_id":"debug-user","photos":["$DefaultMockImageUrl","$DefaultMockImageUrl"]}"""
		),
		DebugEndpointDefinition(
			title = "Upload Dating Photo",
			method = HttpMethod.Post,
			path = "/dating-profile/me/photos",
			defaultStatusCode = 200,
			defaultBody = """{"photo_url":"$DefaultMockImageUrl"}"""
		),
		DebugEndpointDefinition(
			title = "Get Recommendations",
			method = HttpMethod.Get,
			path = "/recommendations",
			defaultStatusCode = 200,
			defaultBody = """{"items":[{"ml_recommendation_id":"rec-1","user_id":"debug-user","candidate_user_id":"candidate-1","reasons":{"lifestyle":0.98},"candidate_card":{"user_id":"candidate-1","profile":{"first_name":"Anna","last_name":"Smirnova","birth_date":"1997-04-03","region":"Saint Petersburg","avatar_url":"$DefaultMockImageUrl"},"dating_profile":{"photos":["$DefaultMockImageUrl"]}}}]}"""
		),
		DebugEndpointDefinition(
			title = "Create Interaction",
			method = HttpMethod.Post,
			path = "/interactions",
			defaultStatusCode = 200,
			defaultBody = """{"interaction_id":"interaction-1","actor_user_id":"debug-user","candidate_user_id":"candidate-1","action":"like","created_at":"2026-03-15T12:00:00Z","ml_recommendation_id":"rec-1"}"""
		)
	)
}
