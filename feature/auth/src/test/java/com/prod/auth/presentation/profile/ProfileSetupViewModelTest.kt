package com.prod.auth.presentation.profile

import com.prod.auth.domain.gateway.AuthGateway
import com.prod.auth.domain.model.Gender
import com.prod.auth.domain.model.JuryPreviewUser
import com.prod.auth.domain.model.Profile
import com.prod.auth.domain.model.ProfileDraft
import com.prod.auth.domain.usecase.CreateJuryProfileDraftUseCase
import com.prod.auth.domain.usecase.GetOwnProfileUseCase
import com.prod.auth.domain.usecase.UpsertOwnProfileUseCase
import com.prod.auth.domain.usecase.UploadDatingPhotoUseCase
import com.prod.auth.presentation.common.AuthOperationResultUiMapper
import com.prod.common.api.model.ApiOperationResult
import com.prod.common.api.model.UserSession
import com.prod.profile.domain.gateway.ProfileGateway
import com.prod.profile.domain.model.DatingProfile
import com.prod.profile.domain.model.DatingProfileDraft
import com.prod.profile.domain.model.OwnCard
import com.prod.profile.domain.model.UserCard
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileSetupViewModelTest {

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
	fun `initialize routes forward when account profile already exists`() = runTest(dispatcher) {
		val authGateway = FakeAuthGateway(
			ownProfileResult = ApiOperationResult.Success(
				Profile(
					userId = "user-1",
					firstName = "Anna",
					lastName = null,
					birthDate = "1997-04-03",
					gender = Gender.Female,
					region = "Saint Petersburg",
					avatarUrl = null
				)
			)
		)
		val viewModel = createViewModel(authGateway = authGateway)
		var profileReadyCalls = 0

		viewModel.initialize(
			onProfileReady = { profileReadyCalls += 1 },
			onNoInternet = {},
			onUnauthorized = {}
		)
		advanceUntilIdle()

		assertEquals(1, profileReadyCalls)
		assertFalse(viewModel.uiState.isCheckingProfile)
	}

	@Test
	fun `submit saves trimmed profile draft without avatar`() = runTest(dispatcher) {
		val authGateway = FakeAuthGateway(
			ownProfileResult = ApiOperationResult.Success(null),
			upsertProfileResult = ApiOperationResult.Success(
				Profile(
					userId = "user-1",
					firstName = "Anna",
					lastName = "Smirnova",
					birthDate = "1997-04-03",
					gender = Gender.Female,
					region = "Saint Petersburg",
					avatarUrl = null
				)
			)
		)
		val viewModel = createViewModel(authGateway = authGateway)
		var successCalls = 0

		viewModel.onFirstNameChange("  Anna  ")
		viewModel.onLastNameChange("  Smirnova ")
		viewModel.onBirthDateSelected("03041997")
		viewModel.onGenderSelected(Gender.Female)
		viewModel.onRegionChange(" Saint Petersburg ")

		viewModel.submit(
			avatarBitmap = null,
			onSuccess = { successCalls += 1 },
			onNoInternet = {},
			onUnauthorized = {}
		)
		advanceUntilIdle()

		assertEquals(1, successCalls)
		assertEquals(
			ProfileDraft(
				firstName = "Anna",
				lastName = "Smirnova",
				birthDate = "1997-04-03",
				gender = Gender.Female,
				region = "Saint Petersburg",
				avatarUrl = null
			),
			authGateway.lastUpsertDraft
		)
		assertFalse(viewModel.uiState.isSubmitting)
		assertTrue(authGateway.uploadPhotoCalls == 0)
	}

	private fun createViewModel(
		authGateway: FakeAuthGateway
	): ProfileSetupViewModel {
		return ProfileSetupViewModel(
			getOwnProfileUseCase = GetOwnProfileUseCase(authGateway),
			upsertOwnProfileUseCase = UpsertOwnProfileUseCase(authGateway),
			uploadDatingPhotoUseCase = UploadDatingPhotoUseCase(FakeProfileGateway()),
			createJuryProfileDraftUseCase = CreateJuryProfileDraftUseCase(),
			authOperationResultUiMapper = AuthOperationResultUiMapper()
		)
	}

	private class FakeAuthGateway(
		private val ownProfileResult: ApiOperationResult<Profile?>,
		private val upsertProfileResult: ApiOperationResult<Profile> = ApiOperationResult.ValidationError()
	) : AuthGateway {
		var lastUpsertDraft: ProfileDraft? = null
		var uploadPhotoCalls: Int = 0

		override suspend fun signInWithEmail(
			email: String,
			password: String
		) = ApiOperationResult.ValidationError()

		override suspend fun signUpWithEmail(
			email: String,
			password: String
		) = ApiOperationResult.ValidationError()

		override suspend fun getJuryPreviewUsers(): ApiOperationResult<List<JuryPreviewUser>> =
			ApiOperationResult.Success(emptyList())

		override suspend fun getOwnProfile(): ApiOperationResult<Profile?> = ownProfileResult

		override suspend fun upsertOwnProfile(draft: ProfileDraft): ApiOperationResult<Profile> {
			lastUpsertDraft = draft
			return upsertProfileResult
		}
	}

	private class FakeProfileGateway : ProfileGateway {
		override suspend fun getOwnCard(): ApiOperationResult<OwnCard> = ApiOperationResult.ValidationError()

		override suspend fun getUserCard(userId: String): ApiOperationResult<UserCard> =
			ApiOperationResult.ValidationError()

		override suspend fun getOwnDatingProfile(): ApiOperationResult<DatingProfile?> =
			ApiOperationResult.Success(null)

		override suspend fun upsertOwnDatingProfile(draft: DatingProfileDraft) =
			ApiOperationResult.ValidationError()

		override suspend fun uploadDatingProfilePhoto(
			contentType: String,
			bytes: ByteArray
		): ApiOperationResult<String> = ApiOperationResult.ValidationError()
	}
}
