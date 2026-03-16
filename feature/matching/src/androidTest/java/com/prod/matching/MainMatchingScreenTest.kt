package com.prod.matching

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import com.prod.common.api.model.ApiOperationResult
import com.prod.match.domain.gateway.MatchGateway
import com.prod.match.domain.model.IncomingLike
import com.prod.match.domain.model.Interaction
import com.prod.match.domain.model.InteractionAction
import com.prod.match.domain.model.Recommendation
import com.prod.match.domain.model.RecommendationCandidateCard
import com.prod.match.domain.model.RecommendationCandidateDatingProfile
import com.prod.match.domain.model.RecommendationCandidateProfile
import com.prod.match.domain.model.TargetGender
import com.prod.match.domain.model.TargetingPreferences
import com.prod.match.domain.model.TargetingPreferencesDraft
import com.prod.matching.domain.gateway.PendingMatchActionStore
import com.prod.matching.domain.model.PendingMatchAction
import com.prod.matching.domain.usecase.GetMatchingFeedUseCase
import com.prod.matching.domain.usecase.GetRecommendationsUseCase
import com.prod.matching.domain.usecase.SubmitMatchActionUseCase
import com.prod.matching.presentation.MainMatchingViewModel
import com.prod.matching.presentation.RecommendationUiMapper
import com.prod.matching.screens.MainMatchingScreen
import com.prod.ui.theme.TmatchTheme
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test

class MainMatchingScreenTest {

	@get:Rule
	val composeRule = createAndroidComposeRule<ComponentActivity>()

	@Test
	fun transitionCardAdvancesOnSwipeLeft() {
		setMatchingContent(startAtTransition = true)

		openTransitionCard()

		composeRule.onNodeWithTag("matching_transition_card")
			.performTouchInput { swipeLeft() }

		waitUntilTagExists("matching_profile_card_recommendation-1")
	}

	@Test
	fun transitionCardAdvancesOnSwipeRight() {
		setMatchingContent(startAtTransition = true)

		openTransitionCard()

		composeRule.onNodeWithTag("matching_transition_card")
			.performTouchInput { swipeRight() }

		waitUntilTagExists("matching_profile_card_recommendation-1")
	}

	private fun setMatchingContent(
		startAtTransition: Boolean
	) {
		val gateway = FakeMatchGateway(
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
						candidateUserId = "recommendation-1",
						firstName = "Bob",
						recommendationId = "rec-1"
					)
				)
			)
		)
		val viewModel = MainMatchingViewModel(
			getMatchingFeedUseCase = GetMatchingFeedUseCase(gateway),
			getRecommendationsUseCase = GetRecommendationsUseCase(gateway),
			submitMatchActionUseCase = SubmitMatchActionUseCase(gateway),
			recommendationUiMapper = RecommendationUiMapper(),
			pendingMatchActionStore = FakePendingMatchActionStore()
		)
		runBlocking {
			viewModel.initialize(
				onUnauthorized = {},
				onNoInternet = {}
			)
		}
		if (startAtTransition) {
			viewModel.onSwipeRight()
		}

		composeRule.setContent {
			TmatchTheme {
				MainMatchingScreen(
					onUnauthorized = {},
					onRequestNoInternet = {},
					onOpenProfile = {},
					onProfileActionHandlerReady = {},
					viewModel = viewModel
				)
			}
		}
		composeRule.waitForIdle()
	}

	private fun openTransitionCard() {
		waitUntilTagExists("matching_transition_card")
		composeRule.onNodeWithText("Входящие лайки закончились").assertIsDisplayed()
	}

	private fun waitUntilTagExists(tag: String) {
		composeRule.waitUntil(timeoutMillis = 15_000) {
			tagExists(tag)
		}
	}

	private fun tagExists(tag: String): Boolean {
		return runCatching {
			composeRule.onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty()
		}.getOrDefault(false)
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

		override suspend fun getOwnTargeting(): ApiOperationResult<TargetingPreferences> {
			return ApiOperationResult.Success(
				TargetingPreferences(
					region = null,
					genderTarget = TargetGender.Both,
					ageFrom = 18,
					ageTo = 30
				)
			)
		}

		override suspend fun upsertOwnTargeting(
			draft: TargetingPreferencesDraft
		): ApiOperationResult<TargetingPreferences> {
			return ApiOperationResult.Success(
				TargetingPreferences(
					region = draft.region,
					genderTarget = draft.genderTarget,
					ageFrom = draft.ageFrom,
					ageTo = draft.ageTo
				)
			)
		}

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
