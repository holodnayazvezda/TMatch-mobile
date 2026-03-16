package com.prod.chats.presentation

import com.prod.chats.domain.gateway.ChatGateway
import com.prod.chats.domain.model.ChatPreview
import com.prod.chats.domain.usecase.GetOwnChatsUseCase
import com.prod.common.api.model.ApiOperationResult
import com.prod.profile.domain.model.UserCard
import com.prod.profile.domain.model.UserCardDatingProfile
import com.prod.profile.domain.model.UserCardProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
	fun `initialize loads chats into ui state`() = runTest(dispatcher) {
		val viewModel = ChatsViewModel(
			getOwnChatsUseCase = GetOwnChatsUseCase(
				chatGateway = object : ChatGateway {
					override suspend fun getOwnChats(): ApiOperationResult<List<ChatPreview>> {
						return ApiOperationResult.Success(
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
					}
				}
			),
			chatPreviewUiMapper = ChatPreviewUiMapper()
		)

		viewModel.initialize(
			onUnauthorized = {},
			onNoInternet = {}
		)
		dispatcher.scheduler.advanceUntilIdle()

		assertEquals(1, viewModel.uiState.items.size)
		assertEquals("Привет", viewModel.uiState.items.single().subtitle)
		assertEquals("Алиса Иванова", viewModel.uiState.items.single().title)
	}

	@Test
	fun `initialize exposes validation error`() = runTest(dispatcher) {
		val viewModel = ChatsViewModel(
			getOwnChatsUseCase = GetOwnChatsUseCase(
				chatGateway = object : ChatGateway {
					override suspend fun getOwnChats(): ApiOperationResult<List<ChatPreview>> {
						return ApiOperationResult.ValidationError("Ошибка")
					}
				}
			),
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
