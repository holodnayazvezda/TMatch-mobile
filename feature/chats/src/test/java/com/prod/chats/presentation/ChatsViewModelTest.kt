package com.prod.chats.presentation

import com.prod.chats.domain.gateway.ChatGateway
import com.prod.chats.domain.model.ChatMessage
import com.prod.chats.domain.model.ChatPreview
import com.prod.chats.domain.usecase.EnsureChatStreamConnectedUseCase
import com.prod.chats.domain.usecase.ObserveOwnChatsUseCase
import com.prod.chats.domain.usecase.RefreshOwnChatsUseCase
import com.prod.common.api.model.ApiOperationResult
import com.prod.profile.domain.model.UserCard
import com.prod.profile.domain.model.UserCardDatingProfile
import com.prod.profile.domain.model.UserCardProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ChatsViewModelTest {

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
	fun `initialize renders chats from db flow and refreshes network`() = runTest(dispatcher) {
		val cachedChats = MutableStateFlow(
			listOf(
				ChatPreview(
					chatId = "chat-1",
					otherUserId = "user-12345678",
					lastMessageId = "message-1",
					lastMessageText = "Привет",
					lastMessageCreatedAt = "2026-03-16T10:15:00+03:00",
					otherUserCard = UserCard(
						userId = "user-12345678",
						profile = UserCardProfile(
							firstName = "Алиса",
							lastName = "Иванова",
							birthDate = "2000-01-01",
							gender = "female",
							region = "Москва",
							avatarUrl = "https://example.com/avatar.jpg"
						),
						datingProfile = UserCardDatingProfile(
							photos = listOf("https://example.com/photo.jpg"),
							traits = emptyList()
						)
					)
				)
			)
		)
		var refreshCalls = 0
		val gateway = object : ChatGateway {
			override fun observeOwnChats(): Flow<List<ChatPreview>> = cachedChats

			override fun observeChat(chatId: String): Flow<ChatPreview?> = MutableStateFlow(null)

			override fun observeChatMessages(chatId: String): Flow<List<ChatMessage>> = MutableStateFlow(emptyList())

			override suspend fun refreshOwnChats(): ApiOperationResult<Unit> {
				refreshCalls += 1
				return ApiOperationResult.Success(Unit)
			}

			override suspend fun refreshChatMessages(
				chatId: String,
				limit: Int,
				beforeMessageId: String?
			): ApiOperationResult<List<ChatMessage>> = ApiOperationResult.Success(emptyList())

			override suspend fun sendMessage(
				chatId: String,
				text: String
			): ApiOperationResult<ChatMessage> = ApiOperationResult.ValidationError()

			override suspend fun ensureChatStreamConnected(): ApiOperationResult<Unit> = ApiOperationResult.Success(Unit)

			override suspend fun clearLocalData() = Unit
		}
		val viewModel = ChatsViewModel(
			observeOwnChatsUseCase = ObserveOwnChatsUseCase(gateway),
			refreshOwnChatsUseCase = RefreshOwnChatsUseCase(gateway),
			ensureChatStreamConnectedUseCase = EnsureChatStreamConnectedUseCase(gateway),
			chatPreviewUiMapper = ChatPreviewUiMapper()
		)

		viewModel.initialize(
			onUnauthorized = {},
			onNoInternet = {}
		)
		dispatcher.scheduler.advanceUntilIdle()

		assertEquals(1, refreshCalls)
		assertEquals(1, viewModel.uiState.items.size)
		assertEquals("Привет", viewModel.uiState.items.single().subtitle)
		assertEquals("Алиса Иванова", viewModel.uiState.items.single().title)
	}

	@Test
	fun `refresh validation error is exposed when db is empty`() = runTest(dispatcher) {
		val gateway = object : ChatGateway {
			override fun observeOwnChats(): Flow<List<ChatPreview>> = MutableStateFlow(emptyList())

			override fun observeChat(chatId: String): Flow<ChatPreview?> = MutableStateFlow(null)

			override fun observeChatMessages(chatId: String): Flow<List<ChatMessage>> = MutableStateFlow(emptyList())

			override suspend fun refreshOwnChats(): ApiOperationResult<Unit> {
				return ApiOperationResult.ValidationError("Ошибка")
			}

			override suspend fun refreshChatMessages(
				chatId: String,
				limit: Int,
				beforeMessageId: String?
			): ApiOperationResult<List<ChatMessage>> = ApiOperationResult.Success(emptyList())

			override suspend fun sendMessage(
				chatId: String,
				text: String
			): ApiOperationResult<ChatMessage> = ApiOperationResult.ValidationError()

			override suspend fun ensureChatStreamConnected(): ApiOperationResult<Unit> = ApiOperationResult.Success(Unit)

			override suspend fun clearLocalData() = Unit
		}
		val viewModel = ChatsViewModel(
			observeOwnChatsUseCase = ObserveOwnChatsUseCase(gateway),
			refreshOwnChatsUseCase = RefreshOwnChatsUseCase(gateway),
			ensureChatStreamConnectedUseCase = EnsureChatStreamConnectedUseCase(gateway),
			chatPreviewUiMapper = ChatPreviewUiMapper()
		)

		viewModel.initialize(
			onUnauthorized = {},
			onNoInternet = {}
		)
		dispatcher.scheduler.advanceUntilIdle()

		assertEquals("Ошибка", viewModel.uiState.error)
		assertTrue(viewModel.uiState.items.isEmpty())
	}
}
