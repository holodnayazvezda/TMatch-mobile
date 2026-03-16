package com.prod.matching.presentation

import com.prod.common.api.model.ApiOperationResult
import com.prod.match.domain.gateway.MatchGateway
import com.prod.match.domain.model.IncomingLike
import com.prod.match.domain.model.Interaction
import com.prod.match.domain.model.InteractionAction
import com.prod.match.domain.model.Recommendation
import com.prod.match.domain.model.RecommendationCandidateCard
import com.prod.match.domain.model.RecommendationCandidateDatingProfile
import com.prod.match.domain.model.RecommendationCandidateProfile
import com.prod.matching.domain.gateway.PendingMatchActionStore
import com.prod.matching.domain.model.PendingMatchAction
import com.prod.matching.domain.usecase.GetMatchingFeedUseCase
import com.prod.matching.domain.usecase.GetRecommendationsUseCase
import com.prod.matching.domain.usecase.SubmitMatchActionUseCase
import com.prod.matching.model.MatchingDeckItem
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

@OptIn(ExperimentalCoroutinesApi::class)
class MainMatchingViewModelTest {

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
	fun `initialize shows incoming likes first inserts transition and filters duplicate recommendations`() = runTest {
		val viewModel = createViewModel(
			matchGateway = FakeMatchGateway(
				incomingLikesResult = ApiOperationResult.Success(
					listOf(
						incomingLike(
							likerUserId = "incoming-1",
							firstName = "Alice"
						)
					)
				),
				recommendationsResult = ApiOperationResult.Success(
					listOf(
						recommendation(
							candidateUserId = "incoming-1",
							firstName = "Alice duplicate",
							recommendationId = "rec-duplicate"
						),
						recommendation(
							candidateUserId = "recommendation-1",
							firstName = "Bob",
							recommendationId = "rec-1"
						)
					)
				)
			)
		)

		viewModel.initialize(
			onUnauthorized = {},
			onNoInternet = {}
		)

		advanceUntilIdle()

		assertFalse(viewModel.uiState.isLoading)
		assertNull(viewModel.uiState.error)
		assertEquals(3, viewModel.uiState.items.size)
		assertEquals("incoming-1", (viewModel.uiState.items[0] as MatchingDeckItem.Profile).id)
		assertEquals(MatchingDeckItem.Transition(), viewModel.uiState.items[1])
		assertEquals("recommendation-1", (viewModel.uiState.items[2] as MatchingDeckItem.Profile).id)
	}

	@Test
	fun `initialize omits transition when recommendations are absent`() = runTest {
		val viewModel = createViewModel(
			matchGateway = FakeMatchGateway(
				incomingLikesResult = ApiOperationResult.Success(
					listOf(
						incomingLike(
							likerUserId = "incoming-1",
							firstName = "Alice"
						)
					)
				),
				recommendationsResult = ApiOperationResult.Success(emptyList())
			)
		)

		viewModel.initialize(
			onUnauthorized = {},
			onNoInternet = {}
		)

		advanceUntilIdle()

		assertEquals(1, viewModel.uiState.items.size)
		assertEquals("incoming-1", viewModel.uiState.currentProfile?.id)
	}

	private fun createViewModel(
		matchGateway: MatchGateway
	): MainMatchingViewModel {
		return MainMatchingViewModel(
			getMatchingFeedUseCase = GetMatchingFeedUseCase(matchGateway),
			getRecommendationsUseCase = GetRecommendationsUseCase(matchGateway),
			submitMatchActionUseCase = SubmitMatchActionUseCase(matchGateway),
			recommendationUiMapper = RecommendationUiMapper(),
			pendingMatchActionStore = FakePendingMatchActionStore()
		)
	}

	private class FakePendingMatchActionStore : PendingMatchActionStore {
		private val actions = mutableListOf<PendingMatchAction>()

		override fun getAll(): List<PendingMatchAction> = actions.toList()

		override fun saveAll(actions: List<PendingMatchAction>) {
			this.actions.clear()
			this.actions.addAll(actions)
		}

		override fun clear() {
			actions.clear()
		}
	}

	private class FakeMatchGateway(
		private val incomingLikesResult: ApiOperationResult<List<IncomingLike>>,
		private val recommendationsResult: ApiOperationResult<List<Recommendation>>,
		private val interactionResult: ApiOperationResult<Interaction> = ApiOperationResult.Success(
			Interaction(
				interactionId = "interaction-1",
				actorUserId = "me",
				candidateUserId = "candidate-1",
				action = InteractionAction.Like,
				createdAt = "2026-03-16T12:00:00Z",
				mlRecommendationId = "rec-1"
			)
		)
	) : MatchGateway {
		override suspend fun getIncomingLikes(): ApiOperationResult<List<IncomingLike>> = incomingLikesResult

		override suspend fun getRecommendations(): ApiOperationResult<List<Recommendation>> = recommendationsResult

		override suspend fun createInteraction(
			candidateUserId: String,
			action: InteractionAction,
			mlRecommendationId: String?
		): ApiOperationResult<Interaction> = interactionResult
	}

	private fun incomingLike(
		likerUserId: String,
		firstName: String
	): IncomingLike {
		return IncomingLike(
			likerUserId = likerUserId,
			candidateCard = RecommendationCandidateCard(
				userId = likerUserId,
				profile = RecommendationCandidateProfile(
					firstName = firstName,
					lastName = null,
					birthDate = "1998-01-01",
					region = null,
					avatarUrl = null
				),
				datingProfile = RecommendationCandidateDatingProfile(
					photos = emptyList(),
					traits = emptyList()
				)
			)
		)
	}

	private fun recommendation(
		candidateUserId: String,
		firstName: String,
		recommendationId: String
	): Recommendation {
		return Recommendation(
			mlRecommendationId = recommendationId,
			userId = "me",
			candidateUserId = candidateUserId,
			reasons = emptyList(),
			candidateCard = RecommendationCandidateCard(
				userId = candidateUserId,
				profile = RecommendationCandidateProfile(
					firstName = firstName,
					lastName = null,
					birthDate = "1997-01-01",
					region = null,
					avatarUrl = null
				),
				datingProfile = RecommendationCandidateDatingProfile(
					photos = emptyList(),
					traits = emptyList()
				)
			)
		)
	}
}
