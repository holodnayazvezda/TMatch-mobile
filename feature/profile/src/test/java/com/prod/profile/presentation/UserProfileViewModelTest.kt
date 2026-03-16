package com.prod.profile.presentation

import com.prod.common.api.model.ApiOperationResult
import com.prod.profile.domain.gateway.ProfileGateway
import com.prod.profile.domain.model.DatingProfile
import com.prod.profile.domain.model.DatingProfileDraft
import com.prod.profile.domain.model.OwnCard
import com.prod.profile.domain.model.UserCard
import com.prod.profile.domain.model.UserCardDatingProfile
import com.prod.profile.domain.model.UserCardDatingTrait
import com.prod.profile.domain.model.UserCardProfile
import com.prod.profile.domain.usecase.GetUserCardUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
import java.time.LocalDate
import java.time.Period

@OptIn(ExperimentalCoroutinesApi::class)
class UserProfileViewModelTest {

	private val testDispatcher = StandardTestDispatcher()

	@Before
	fun setUp() {
		Dispatchers.setMain(testDispatcher)
	}

	@After
	fun tearDown() {
		Dispatchers.resetMain()
	}

	@Test
	fun `initialize maps loaded card to ui state with visible sorted traits`() = runTest {
		val viewModel = UserProfileViewModel(
			getUserCardUseCase = GetUserCardUseCase(
				profileGateway = FakeProfileGateway(
					result = ApiOperationResult.Success(
						UserCard(
							userId = "candidate-1",
							profile = UserCardProfile(
								firstName = "Anna",
								lastName = "Smirnova",
								birthDate = "1997-04-03",
								gender = "female",
								region = "Saint Petersburg",
								avatarUrl = "https://example.com/avatar.jpg"
							),
							datingProfile = UserCardDatingProfile(
								photos = listOf(
									"https://example.com/photo-1.jpg",
									"https://example.com/photo-2.jpg"
								),
								traits = listOf(
									UserCardDatingTrait(
										traitCode = "active_lifestyle",
										score = 0.81f,
										isHidden = false
									),
									UserCardDatingTrait(
										traitCode = "kind",
										score = 0.93f,
										isHidden = false
									),
									UserCardDatingTrait(
										traitCode = "kind",
										score = 0.40f,
										isHidden = false
									),
									UserCardDatingTrait(
										traitCode = "secret_trait",
										score = 1.0f,
										isHidden = true
									)
								)
							)
						)
					)
				)
			)
		)
		var unauthorizedCalled = false
		var noInternetCalled = false

		viewModel.initialize(
			userId = "candidate-1",
			onUnauthorized = { unauthorizedCalled = true },
			onNoInternet = { noInternetCalled = true }
		)

		advanceUntilIdle()

		val uiState = viewModel.uiState
		assertFalse(uiState.isLoading)
		assertNull(uiState.error)
		assertFalse(unauthorizedCalled)
		assertFalse(noInternetCalled)
		assertEquals("candidate-1", uiState.profileCard?.userId)
		assertEquals("Anna Smirnova", uiState.profileCard?.fullName)
		assertEquals(
			Period.between(LocalDate.parse("1997-04-03"), LocalDate.now()).years,
			uiState.profileCard?.age
		)
		assertEquals("03.04.1997", uiState.profileCard?.birthDateLabel)
		assertEquals("Saint Petersburg", uiState.profileCard?.region)
		assertEquals("https://example.com/avatar.jpg", uiState.profileCard?.avatarUrl)
		assertEquals(
			listOf("https://example.com/photo-1.jpg", "https://example.com/photo-2.jpg"),
			uiState.profileCard?.datingPhotoUrls
		)
		assertEquals(
			listOf("Kind", "Active Lifestyle"),
			uiState.profileCard?.visibleTraitLabels
		)
	}

	private class FakeProfileGateway(
		private val result: ApiOperationResult<UserCard>
	) : ProfileGateway {
		override suspend fun getOwnCard(): ApiOperationResult<OwnCard> = error("Not used in this test")

		override suspend fun getUserCard(userId: String): ApiOperationResult<UserCard> = result

		override suspend fun getOwnDatingProfile(): ApiOperationResult<DatingProfile?> = error("Not used in this test")

		override suspend fun upsertOwnDatingProfile(draft: DatingProfileDraft) =
			error("Not used in this test")

		override suspend fun uploadDatingProfilePhoto(
			contentType: String,
			bytes: ByteArray
		) = error("Not used in this test")
	}
}
