package com.prod.t_match.debug

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.prod.tmatch_api.common.data.network.ApiResponseOverride
import com.prod.tmatch_api.common.data.network.ApiResponseOverrideRegistry
import com.prod.ui.components.buttons.AppBigButton
import com.prod.ui.components.buttons.AppMediumButton
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
						body = override?.body ?: definition.defaultBody,
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
		Text(
			text = "Debug API Overrides",
			style = MaterialTheme.typography.headlineMedium,
			fontWeight = FontWeight.SemiBold
		)
		Text(
			text = "Для каждой ручки можно включить override, задать HTTP-статус и тело ответа. Если override выключен, запрос идет в реальный backend.",
			style = MaterialTheme.typography.bodyMedium,
			color = MaterialTheme.colorScheme.onSurfaceVariant
		)

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
							body = definition.defaultBody,
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
					} else {
						registry.removeOverride(
							method = state.definition.method,
							path = state.definition.path
						)
					}

					editorStates[index] = state.copy(error = null)
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
								error = null
							)
						)
					}
				)
			}

			OutlinedTextField(
				value = state.statusCode,
				onValueChange = { value ->
					onStateChange(
						state.copy(
							statusCode = value.filter { it.isDigit() },
							error = null
						)
					)
				},
				modifier = Modifier.fillMaxWidth(),
				label = { Text("HTTP status") },
				singleLine = true
			)

			OutlinedTextField(
				value = state.body,
				onValueChange = { value ->
					onStateChange(
						state.copy(
							body = value,
							error = null
						)
					)
				},
				modifier = Modifier.fillMaxWidth(),
				label = { Text("Response body") },
				minLines = 6
			)

			state.error?.let { error ->
				Text(
					text = error,
					style = MaterialTheme.typography.bodySmall,
					color = MaterialTheme.colorScheme.error
				)
			}

			AppBigButton(
				onClick = onApply,
				buttonText = if (state.enabled) "Сохранить override" else "Выключить override",
				isLoading = false
			)
		}
	}
}

private data class EndpointOverrideEditorState(
	val definition: DebugEndpointDefinition,
	val enabled: Boolean,
	val statusCode: String,
	val body: String,
	val error: String?
)

private data class DebugEndpointDefinition(
	val title: String,
	val method: HttpMethod,
	val path: String,
	val defaultStatusCode: Int,
	val defaultBody: String
)

private fun debugEndpointDefinitions(): List<DebugEndpointDefinition> {
	return listOf(
		DebugEndpointDefinition(
			title = "Sign In",
			method = HttpMethod.Post,
			path = "/auth/signing/email",
			defaultStatusCode = 200,
			defaultBody = """{"accessToken":"debug-token","userId":"debug-user"}"""
		),
		DebugEndpointDefinition(
			title = "Sign Up",
			method = HttpMethod.Post,
			path = "/auth/registration/email",
			defaultStatusCode = 200,
			defaultBody = """{"accessToken":"debug-token","userId":"debug-user"}"""
		),
		DebugEndpointDefinition(
			title = "Get Own Profile",
			method = HttpMethod.Get,
			path = "/profile",
			defaultStatusCode = 200,
			defaultBody = """{"userId":"debug-user","firstName":"Max","lastName":"Ivanov","birthDate":"1995-01-01","region":"Moscow","avatarUrl":"https://example.com/avatar.png"}"""
		),
		DebugEndpointDefinition(
			title = "Upsert Own Profile",
			method = HttpMethod.Put,
			path = "/profile",
			defaultStatusCode = 200,
			defaultBody = """{"userId":"debug-user","firstName":"Max","lastName":"Ivanov","birthDate":"1995-01-01","region":"Moscow","avatarUrl":"https://example.com/avatar.png"}"""
		),
		DebugEndpointDefinition(
			title = "Get Dating Profile",
			method = HttpMethod.Get,
			path = "/dating-profile/me",
			defaultStatusCode = 200,
			defaultBody = """{"userId":"debug-user","photos":["https://example.com/photo-1.png","https://example.com/photo-2.png"]}"""
		),
		DebugEndpointDefinition(
			title = "Upsert Dating Profile",
			method = HttpMethod.Put,
			path = "/dating-profile/me",
			defaultStatusCode = 200,
			defaultBody = """{"userId":"debug-user","photos":["https://example.com/photo-1.png","https://example.com/photo-2.png"]}"""
		),
		DebugEndpointDefinition(
			title = "Upload Dating Photo",
			method = HttpMethod.Post,
			path = "/dating-profile/me/photos",
			defaultStatusCode = 200,
			defaultBody = """{"photoUrl":"https://example.com/uploaded-photo.png"}"""
		),
		DebugEndpointDefinition(
			title = "Get Recommendations",
			method = HttpMethod.Get,
			path = "/recommendations",
			defaultStatusCode = 200,
			defaultBody = """{"items":[{"mlRecommendationId":"rec-1","userId":"debug-user","candidateUserId":"candidate-1","reasons":[{"lifestyle":0.98}],"candidateCard":{"userId":"candidate-1","profile":{"firstName":"Anna","lastName":"Smirnova","birthDate":"1997-04-03","region":"Saint Petersburg","avatarUrl":"https://example.com/candidate-avatar.png"},"datingProfile":{"photos":["https://example.com/candidate-photo.png"]}}}]}"""
		),
		DebugEndpointDefinition(
			title = "Create Interaction",
			method = HttpMethod.Post,
			path = "/interactions",
			defaultStatusCode = 200,
			defaultBody = """{"interactionId":"interaction-1","actorUserId":"debug-user","candidateUserId":"candidate-1","action":"like","createdAt":"2026-03-15T12:00:00Z","mlRecommendationId":"rec-1"}"""
		)
	)
}
