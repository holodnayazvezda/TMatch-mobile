package com.prod.chats.presentation

import com.prod.auth.domain.gateway.AuthSessionStore
import com.prod.chats.domain.gateway.ChatGateway
import com.prod.chats.domain.model.ChatMessage
import com.prod.chats.domain.model.ChatPreview
import com.prod.chats.domain.usecase.EnsureChatStreamConnectedUseCase
import com.prod.chats.domain.usecase.ObserveChatMessagesUseCase
import com.prod.chats.domain.usecase.ObserveChatUseCase
import com.prod.chats.domain.usecase.RefreshChatMessagesUseCase
import com.prod.chats.domain.usecase.SendChatMessageUseCase
import com.prod.common.api.model.ApiOperationResult
import com.prod.common.api.model.UserSession
import com.prod.profile.domain.model.UserCard
import com.prod.profile.domain.model.UserCardDatingProfile
import com.prod.profile.domain.model.UserCardProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ChatViewModelTest {

	private val dispatcher = StandardTestDispatcher()

	@Before
	fun setUp() {
		Dispatchers.setMain(dispatcher)
	}

	@After
	fun tearDown() {
		Dispatchers.resetMain()
	}

	@Test
	fun `initialize renders cached chat and messages then refreshes network`() = runTest(dispatcher) {
		val chatFlow = MutableStateFlow<ChatPreview?>(
			ChatPreview(
				chatId = "chat-1",
				otherUserId = "user-2",
				lastMessageId = "message-1",
				lastMessageText = "Привет",
				lastMessageCreatedAt = "2026-03-16T10:15:00+03:00",
				otherUserCard = UserCard(
					userId = "user-2",
					profile = UserCardProfile(
						firstName = "Алиса",
						lastName = "Иванова",
						birthDate = "2000-01-01",
						gender = "female",
						region = "Москва",
						avatarUrl = "https://example.com/avatar.jpg"
					),
					datingProfile = UserCardDatingProfile(
						photos = emptyList(),
						traits = emptyList()
					)
				)
			)
		)
		val messagesFlow = MutableStateFlow(
			listOf(
				ChatMessage(
					messageId = "message-1",
					chatId = "chat-1",
					senderUserId = "user-2",
					text = "Привет",
					createdAt = "2026-03-16T10:15:00+03:00"
				)
			)
		)
		val gateway = FakeChatGateway(
			chatFlow = chatFlow,
			messagesFlow = messagesFlow,
			refreshMessagesResult = ApiOperationResult.Success(messagesFlow.value)
		)
		val viewModel = createViewModel(gateway)

		viewModel.initialize(
			chatId = "chat-1",
			onUnauthorized = {},
			onNoInternet = {}
		)
		advanceUntilIdle()

		assertEquals(1, gateway.ensureConnectedCalls)
		assertEquals(1, gateway.refreshChatMessagesCalls.size)
		assertEquals("Алиса Иванова", viewModel.uiState.title)
		assertEquals("user-2", viewModel.uiState.otherUserId)
		assertEquals(1, viewModel.uiState.messages.size)
		assertEquals("10:15", viewModel.uiState.messages.single().timestamp)
		assertFalse(viewModel.uiState.messages.single().isOwn)
		assertFalse(viewModel.uiState.isLoading)
		assertNull(viewModel.uiState.error)
	}

	@Test
	fun `send message submits trimmed text and clears draft on success`() = runTest(dispatcher) {
		val gateway = FakeChatGateway(
			chatFlow = MutableStateFlow(null),
			messagesFlow = MutableStateFlow(emptyList()),
			sendMessageResult = ApiOperationResult.Success(
				ChatMessage(
					messageId = "message-2",
					chatId = "chat-1",
					senderUserId = "user-1",
					text = "Здравствуйте",
					createdAt = "2026-03-16T11:00:00+03:00"
				)
			)
		)
		val viewModel = createViewModel(gateway)

		viewModel.onDraftChanged("  Здравствуйте  ")
		viewModel.sendMessage(
			chatId = "chat-1",
			onUnauthorized = {},
			onNoInternet = {}
		)
		advanceUntilIdle()

		assertEquals("Здравствуйте", gateway.lastSentText)
		assertEquals("", viewModel.uiState.draftMessage)
		assertFalse(viewModel.uiState.isSending)
	}

	private fun createViewModel(
		gateway: FakeChatGateway
	): ChatViewModel {
		return ChatViewModel(
			authSessionStore = FakeAuthSessionStore(),
			observeChatUseCase = ObserveChatUseCase(gateway),
			observeChatMessagesUseCase = ObserveChatMessagesUseCase(gateway),
			refreshChatMessagesUseCase = RefreshChatMessagesUseCase(gateway),
			sendChatMessageUseCase = SendChatMessageUseCase(gateway),
			ensureChatStreamConnectedUseCase = EnsureChatStreamConnectedUseCase(gateway),
			chatMessageUiMapper = ChatMessageUiMapper()
		)
	}

	private class FakeAuthSessionStore : AuthSessionStore {
		override fun get(): UserSession? = UserSession(accessToken = "token", userId = "user-1")

		override fun save(session: UserSession) = Unit

		override fun isProfileCompleted(): Boolean = false

		override fun setProfileCompleted(isCompleted: Boolean) = Unit

		override fun hasDatingProfile(): Boolean = false

		override fun setHasDatingProfile(hasDatingProfile: Boolean) = Unit

		override fun clear() = Unit
	}

	private class FakeChatGateway(
		private val chatFlow: MutableStateFlow<ChatPreview?>,
		private val messagesFlow: MutableStateFlow<List<ChatMessage>>,
		private val refreshMessagesResult: ApiOperationResult<List<ChatMessage>> = ApiOperationResult.Success(emptyList()),
		private val sendMessageResult: ApiOperationResult<ChatMessage> = ApiOperationResult.ValidationError()
	) : ChatGateway {
		val refreshChatMessagesCalls = mutableListOf<Pair<String, String?>>()
		var ensureConnectedCalls = 0
		var lastSentText: String? = null

		override fun observeOwnChats(): Flow<List<ChatPreview>> = MutableStateFlow(emptyList())

		override fun observeChat(chatId: String): Flow<ChatPreview?> = chatFlow

		override fun observeChatMessages(chatId: String): Flow<List<ChatMessage>> = messagesFlow

		override suspend fun refreshOwnChats(): ApiOperationResult<Unit> = ApiOperationResult.Success(Unit)

		override suspend fun refreshChatMessages(
			chatId: String,
			limit: Int,
			beforeMessageId: String?
		): ApiOperationResult<List<ChatMessage>> {
			refreshChatMessagesCalls += chatId to beforeMessageId
			return refreshMessagesResult
		}

		override suspend fun sendMessage(
			chatId: String,
			text: String
		): ApiOperationResult<ChatMessage> {
			lastSentText = text
			return sendMessageResult
		}

		override suspend fun ensureChatStreamConnected(): ApiOperationResult<Unit> {
			ensureConnectedCalls += 1
			return ApiOperationResult.Success(Unit)
		}

		override suspend fun clearLocalData() = Unit
	}
}
