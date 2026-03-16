package com.prod.tmatch_api.chats.data.gateway

import com.prod.auth.domain.gateway.AuthSessionStore
import com.prod.common.api.model.ApiErrorReason
import com.prod.common.api.model.ApiOperationResult
import com.prod.tmatch_api.chats.data.dto.ChatStreamEventDto
import com.prod.tmatch_api.chats.data.local.ChatMessageEntity
import com.prod.tmatch_api.chats.data.local.ChatPreviewDao
import com.prod.tmatch_api.common.data.network.KtorGatewaySupport
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.http.encodeURLQueryComponent
import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketSession
import io.ktor.websocket.readText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.util.concurrent.atomic.AtomicReference

class KtorChatStreamManager(
	private val httpClient: HttpClient,
	private val authSessionStore: AuthSessionStore,
	private val chatPreviewDao: ChatPreviewDao,
	private val baseUrl: String
) {
	private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
	private val connectionJob = AtomicReference<Job?>(null)
	private val socketSession = AtomicReference<WebSocketSession?>(null)
	private val gatewaySupport = KtorGatewaySupport(authSessionStore)
	private val json = Json {
		ignoreUnknownKeys = true
		explicitNulls = false
	}

	suspend fun ensureConnected(): ApiOperationResult<Unit> {
		val session = authSessionStore.get()
			?: return ApiOperationResult.ApiError(ApiErrorReason.Unauthorized)
		if (connectionJob.get()?.isActive == true) {
			return ApiOperationResult.Success(Unit)
		}

		return try {
			val activeSocketSession = httpClient.webSocketSession(buildSocketUrl(session.accessToken))
			socketSession.set(activeSocketSession)

			val job = scope.launch {
				try {
					activeSocketSession.incoming.consumeAsFlow().collect { frame ->
						if (frame is Frame.Text) {
							handleEvent(frame.readText(), session.userId)
						}
					}
				} catch (cancellation: CancellationException) {
					throw cancellation
				} catch (_: Exception) {
					// The chat screen reads from Room. If the stream drops, we degrade to HTTP polling
					// and allow a future ensureConnected() call to re-establish the socket.
				} finally {
					socketSession.compareAndSet(activeSocketSession, null)
					connectionJob.compareAndSet(connectionJob.get(), null)
				}
			}

			connectionJob.set(job)
			ApiOperationResult.Success(Unit)
		} catch (exception: Exception) {
			connectionJob.set(null)
			socketSession.set(null)
			gatewaySupport.handleException(exception)
		}
	}

	suspend fun disconnect() {
		connectionJob.getAndSet(null)?.cancelAndJoin()
		socketSession.set(null)
	}

	private suspend fun handleEvent(
		payload: String,
		ownerUserId: String
	) {
		val event = runCatching {
			json.decodeFromString(ChatStreamEventDto.serializer(), payload)
		}.getOrNull() ?: return

		if (event.type != "message") return

		chatPreviewDao.insertMessage(event.toEntity(ownerUserId))
		chatPreviewDao.updateChatLastMessage(
			ownerUserId = ownerUserId,
			chatId = event.chatId,
			messageId = event.messageId,
			text = event.text,
			createdAt = event.createdAt
		)
	}

	private fun ChatStreamEventDto.toEntity(ownerUserId: String): ChatMessageEntity {
		return ChatMessageEntity(
			ownerUserId = ownerUserId,
			messageId = messageId,
			chatId = chatId,
			senderUserId = senderUserId,
			text = text,
			createdAt = createdAt
		)
	}

	private fun buildSocketUrl(accessToken: String): String {
		val normalizedBaseUrl = baseUrl.removeSuffix("/")
		val websocketBaseUrl = when {
			normalizedBaseUrl.startsWith("https://") -> {
				"wss://${normalizedBaseUrl.removePrefix("https://")}"
			}

			normalizedBaseUrl.startsWith("http://") -> {
				"ws://${normalizedBaseUrl.removePrefix("http://")}"
			}

			else -> normalizedBaseUrl
		}

		return "$websocketBaseUrl/chat/stream?token=${accessToken.encodeURLQueryComponent()}"
	}
}
