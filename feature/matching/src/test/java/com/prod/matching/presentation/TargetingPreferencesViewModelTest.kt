package com.prod.matching.presentation

import com.prod.common.api.model.ApiOperationResult
import com.prod.match.domain.gateway.MatchGateway
import com.prod.match.domain.model.IncomingLike
import com.prod.match.domain.model.Interaction
import com.prod.match.domain.model.InteractionAction
import com.prod.match.domain.model.Recommendation
import com.prod.match.domain.model.TargetGender
import com.prod.match.domain.model.TargetingPreferences
import com.prod.match.domain.model.TargetingPreferencesDraft
import com.prod.matching.domain.usecase.GetOwnTargetingUseCase
import com.prod.matching.domain.usecase.UpsertOwnTargetingUseCase
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
class TargetingPreferencesViewModelTest {

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
	fun `initialize loads targeting from gateway`() = runTest(dispatcher) {
		val gateway = FakeMatchGateway(
			getOwnTargetingResult = ApiOperationResult.Success(
				TargetingPreferences(
					region = "Москва",
					genderTarget = TargetGender.Female,
					ageFrom = 24,
					ageTo = 31
				)
			)
		)
		val viewModel = createViewModel(gateway)

		viewModel.initialize(
			onUnauthorized = {},
			onNoInternet = {}
		)
		advanceUntilIdle()

		assertFalse(viewModel.uiState.isLoading)
		assertNull(viewModel.uiState.error)
		assertEquals("Москва", viewModel.uiState.region)
		assertEquals(TargetGender.Female, viewModel.uiState.genderTarget)
		assertEquals("24-31", viewModel.uiState.ageRangeLabel)
	}

	@Test
	fun `save persists updated targeting draft`() = runTest(dispatcher) {
		val gateway = FakeMatchGateway(
			getOwnTargetingResult = ApiOperationResult.Success(
				TargetingPreferences(
					region = null,
					genderTarget = TargetGender.Both,
					ageFrom = 18,
					ageTo = 30
				)
			),
			upsertOwnTargetingResult = ApiOperationResult.Success(
				TargetingPreferences(
					region = "Санкт-Петербург",
					genderTarget = TargetGender.Male,
					ageFrom = 26,
					ageTo = 33
				)
			)
		)
		val viewModel = createViewModel(gateway)
		var saveCalls = 0

		viewModel.initialize(
			onUnauthorized = {},
			onNoInternet = {}
		)
		advanceUntilIdle()
		viewModel.onRegionSelected("Санкт-Петербург")
		viewModel.onGenderTargetSelected(TargetGender.Male)
		viewModel.onAgeRangeChanged(ageFrom = 26, ageTo = 33)

		viewModel.save(onSaved = { saveCalls += 1 })
		advanceUntilIdle()

		assertEquals(
			TargetingPreferencesDraft(
				region = "Санкт-Петербург",
				genderTarget = TargetGender.Male,
				ageFrom = 26,
				ageTo = 33
			),
			gateway.lastUpsertDraft
		)
		assertEquals(1, saveCalls)
		assertEquals("Санкт-Петербург", viewModel.uiState.region)
		assertEquals(TargetGender.Male, viewModel.uiState.genderTarget)
	}

	private fun createViewModel(
		gateway: FakeMatchGateway
	): TargetingPreferencesViewModel {
		return TargetingPreferencesViewModel(
			getOwnTargetingUseCase = GetOwnTargetingUseCase(gateway),
			upsertOwnTargetingUseCase = UpsertOwnTargetingUseCase(gateway)
		)
	}

	private class FakeMatchGateway(
		private val getOwnTargetingResult: ApiOperationResult<TargetingPreferences>,
		private val upsertOwnTargetingResult: ApiOperationResult<TargetingPreferences> = getOwnTargetingResult
	) : MatchGateway {
		var lastUpsertDraft: TargetingPreferencesDraft? = null

		override suspend fun getIncomingLikes(): ApiOperationResult<List<IncomingLike>> =
			ApiOperationResult.Success(emptyList())

		override suspend fun getRecommendations(): ApiOperationResult<List<Recommendation>> =
			ApiOperationResult.Success(emptyList())

		override suspend fun getOwnTargeting(): ApiOperationResult<TargetingPreferences> =
			getOwnTargetingResult

		override suspend fun upsertOwnTargeting(
			draft: TargetingPreferencesDraft
		): ApiOperationResult<TargetingPreferences> {
			lastUpsertDraft = draft
			return upsertOwnTargetingResult
		}

		override suspend fun createInteraction(
			candidateUserId: String,
			action: InteractionAction,
			mlRecommendationId: String?
		): ApiOperationResult<Interaction> = ApiOperationResult.ValidationError()
	}
}
