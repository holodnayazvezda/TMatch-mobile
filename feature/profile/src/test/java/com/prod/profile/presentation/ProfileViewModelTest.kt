package com.prod.profile.presentation

import com.prod.auth.domain.gateway.AuthGateway
import com.prod.auth.domain.model.Gender
import com.prod.auth.domain.model.JuryPreviewUser
import com.prod.auth.domain.model.Profile
import com.prod.auth.domain.model.ProfileDraft
import com.prod.common.api.model.ApiOperationResult
import com.prod.common.api.model.UserSession
import com.prod.profile.domain.gateway.ProfileGateway
import com.prod.profile.domain.model.DatingProfile
import com.prod.profile.domain.model.DatingProfileDraft
import com.prod.profile.domain.model.DatingTrait
import com.prod.profile.domain.model.OwnCard
import com.prod.profile.domain.model.UserCard
import com.prod.profile.domain.usecase.GetOwnAccountProfileUseCase
import com.prod.profile.domain.usecase.GetOwnDatingProfileUseCase
import com.prod.profile.domain.usecase.UpsertOwnDatingProfileUseCase
import com.prod.profile.domain.usecase.UploadDatingPhotoUseCase
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
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

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
	fun `initialize maps account and dating profile to ui card`() = runTest(dispatcher) {
		val authGateway = FakeAuthGateway(
			ownProfileResult = ApiOperationResult.Success(
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
		val profileGateway = FakeProfileGateway(
			ownDatingProfileResult = ApiOperationResult.Success(
				DatingProfile(
					userId = "user-1",
					photos = listOf("https://example.com/photo-1.jpg"),
					traits = emptyList()
				)
			)
		)
		val viewModel = createViewModel(authGateway, profileGateway)

		viewModel.initialize(
			onUnauthorized = {},
			onProfileMissing = {},
			onNoInternet = {}
		)
		advanceUntilIdle()

		assertFalse(viewModel.uiState.isLoading)
		assertNull(viewModel.uiState.error)
		assertNotNull(viewModel.uiState.profileCard)
		assertEquals("Anna", viewModel.uiState.profileCard?.firstName)
		assertEquals("Anna Smirnova", viewModel.uiState.profileCard?.fullName)
		assertEquals("03.04.1997", viewModel.uiState.profileCard?.birthDateLabel)
		assertEquals(listOf("https://example.com/photo-1.jpg"), viewModel.uiState.profileCard?.datingPhotoUrls)
	}

	@Test
	fun `replace dating photo uploads file and updates profile photos`() = runTest(dispatcher) {
		val authGateway = FakeAuthGateway(
			ownProfileResult = ApiOperationResult.Success(
				Profile(
					userId = "user-1",
					firstName = "Anna",
					lastName = null,
					birthDate = "1997-04-03",
					gender = Gender.Female,
					region = null,
					avatarUrl = null
				)
			)
		)
		val profileGateway = FakeProfileGateway(
			ownDatingProfileResult = ApiOperationResult.Success(
				DatingProfile(
					userId = "user-1",
					photos = listOf("https://example.com/old.jpg"),
					traits = listOf(DatingTrait("kind", 0.9f, false))
				)
			),
			uploadPhotoResult = ApiOperationResult.Success("https://example.com/new.jpg"),
			upsertDatingProfileResult = ApiOperationResult.Success(
				DatingProfile(
					userId = "user-1",
					photos = listOf("https://example.com/new.jpg"),
					traits = emptyList()
				)
			)
		)
		val viewModel = createViewModel(authGateway, profileGateway)

		viewModel.initialize(
			onUnauthorized = {},
			onProfileMissing = {},
			onNoInternet = {}
		)
		advanceUntilIdle()

		viewModel.replaceDatingPhoto(
			selectedPhotoIndex = 0,
			imageBytes = byteArrayOf(1, 2, 3),
			onUnauthorized = {},
			onNoInternet = {}
		)
		advanceUntilIdle()

		assertEquals(
			DatingProfileDraft(photos = listOf("https://example.com/new.jpg")),
			profileGateway.lastUpsertDraft
		)
		assertEquals(listOf("https://example.com/new.jpg"), viewModel.uiState.profileCard?.datingPhotoUrls)
		assertFalse(viewModel.uiState.isSubmitting)
	}

	private fun createViewModel(
		authGateway: FakeAuthGateway,
		profileGateway: FakeProfileGateway
	): ProfileViewModel {
		return ProfileViewModel(
			getOwnAccountProfileUseCase = GetOwnAccountProfileUseCase(authGateway),
			getOwnDatingProfileUseCase = GetOwnDatingProfileUseCase(profileGateway),
			upsertOwnDatingProfileUseCase = UpsertOwnDatingProfileUseCase(profileGateway),
			uploadDatingPhotoUseCase = UploadDatingPhotoUseCase(profileGateway)
		)
	}

	private class FakeAuthGateway(
		private val ownProfileResult: ApiOperationResult<Profile?>
	) : AuthGateway {
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

		override suspend fun upsertOwnProfile(draft: ProfileDraft) = ApiOperationResult.ValidationError()
	}

	private class FakeProfileGateway(
		private val ownDatingProfileResult: ApiOperationResult<DatingProfile?>,
		private val uploadPhotoResult: ApiOperationResult<String> = ApiOperationResult.ValidationError(),
		private val upsertDatingProfileResult: ApiOperationResult<DatingProfile> = ApiOperationResult.ValidationError()
	) : ProfileGateway {
		var lastUpsertDraft: DatingProfileDraft? = null

		override suspend fun getOwnCard(): ApiOperationResult<OwnCard> = ApiOperationResult.ValidationError()

		override suspend fun getUserCard(userId: String): ApiOperationResult<UserCard> =
			ApiOperationResult.ValidationError()

		override suspend fun getOwnDatingProfile(): ApiOperationResult<DatingProfile?> =
			ownDatingProfileResult

		override suspend fun upsertOwnDatingProfile(
			draft: DatingProfileDraft
		): ApiOperationResult<DatingProfile> {
			lastUpsertDraft = draft
			return upsertDatingProfileResult
		}

		override suspend fun uploadDatingProfilePhoto(
			contentType: String,
			bytes: ByteArray
		): ApiOperationResult<String> = uploadPhotoResult
	}
}
