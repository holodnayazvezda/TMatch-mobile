package com.prod.t_match.di

import com.prod.auth.di.authFeatureModule
import com.prod.auth.domain.gateway.AuthGateway
import com.prod.auth.domain.gateway.AuthSessionStore
import com.prod.auth.domain.model.Gender
import com.prod.auth.domain.model.JuryPreviewUser
import com.prod.auth.domain.model.Profile
import com.prod.auth.domain.model.ProfileDraft
import com.prod.auth.presentation.signin.SignInViewModel
import com.prod.auth.presentation.signup.SignUpViewModel
import com.prod.chats.di.chatsFeatureModule
import com.prod.chats.domain.gateway.ChatGateway
import com.prod.chats.domain.model.ChatPreview
import com.prod.chats.presentation.ChatsViewModel
import com.prod.common.api.model.ApiOperationResult
import com.prod.common.api.model.UserSession
import com.prod.match.domain.gateway.MatchGateway
import com.prod.match.domain.model.IncomingLike
import com.prod.match.domain.model.Interaction
import com.prod.match.domain.model.InteractionAction
import com.prod.match.domain.model.Recommendation
import com.prod.match.domain.model.TargetGender
import com.prod.match.domain.model.TargetingPreferences
import com.prod.match.domain.model.TargetingPreferencesDraft
import com.prod.matching.di.matchingFeatureModule
import com.prod.matching.domain.gateway.PendingMatchActionStore
import com.prod.matching.domain.model.PendingMatchAction
import com.prod.matching.presentation.MainMatchingViewModel
import com.prod.profile.domain.gateway.ProfileGateway
import com.prod.profile.domain.model.DatingProfile
import com.prod.profile.domain.model.DatingProfileDraft
import com.prod.profile.domain.model.OwnCard
import com.prod.profile.domain.model.UserCard
import com.prod.profile.di.profileFeatureModule
import com.prod.profile.presentation.ProfileViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module

class AppModulesSmokeTest {

	@After
	fun tearDown() {
		stopKoin()
	}

	@Test
	fun featureViewModelsResolveFromKoin() {
		val koin = startKoin {
			allowOverride(true)
			modules(
				authFeatureModule,
				chatsFeatureModule,
				profileFeatureModule,
				matchingFeatureModule,
				module {
					single<AuthSessionStore> { FakeAuthSessionStore() }
					single<AuthGateway> { FakeAuthGateway() }
					single<ChatGateway> { FakeChatGateway() }
					single<ProfileGateway> { FakeProfileGateway() }
					single<MatchGateway> { FakeMatchGateway() }
					single<PendingMatchActionStore> { FakePendingMatchActionStore() }
				}
			)
		}.koin

		assertNotNull(koin.get<SignInViewModel>())
		assertNotNull(koin.get<SignUpViewModel>())
		assertNotNull(koin.get<ChatsViewModel>())
		assertNotNull(koin.get<ProfileViewModel>())
		assertNotNull(koin.get<MainMatchingViewModel>())
	}

	private class FakeAuthSessionStore : AuthSessionStore {
		private var session: UserSession? = null
		private var isProfileCompleted: Boolean = false
		private var hasDatingProfile: Boolean = false

		override fun get(): UserSession? = session

		override fun save(session: UserSession) {
			this.session = session
		}

		override fun isProfileCompleted(): Boolean = isProfileCompleted

		override fun setProfileCompleted(isCompleted: Boolean) {
			isProfileCompleted = isCompleted
		}

		override fun hasDatingProfile(): Boolean = hasDatingProfile

		override fun setHasDatingProfile(hasDatingProfile: Boolean) {
			this.hasDatingProfile = hasDatingProfile
		}

		override fun clear() {
			session = null
			isProfileCompleted = false
			hasDatingProfile = false
		}
	}

	private class FakeAuthGateway : AuthGateway {
		override suspend fun signInWithEmail(
			email: String,
			password: String
		): ApiOperationResult<UserSession> = ApiOperationResult.ValidationError()

		override suspend fun signUpWithEmail(
			email: String,
			password: String
		): ApiOperationResult<UserSession> = ApiOperationResult.ValidationError()

		override suspend fun getJuryPreviewUsers(): ApiOperationResult<List<JuryPreviewUser>> =
			ApiOperationResult.Success(emptyList())

		override suspend fun getOwnProfile(): ApiOperationResult<Profile?> =
			ApiOperationResult.Success(null)

		override suspend fun upsertOwnProfile(
			draft: ProfileDraft
		): ApiOperationResult<Profile> = ApiOperationResult.ValidationError()
	}

	private class FakeProfileGateway : ProfileGateway {
		override suspend fun getOwnCard(): ApiOperationResult<OwnCard> {
			return ApiOperationResult.Success(
				OwnCard(
					userId = "user",
					hasAccountProfile = true,
					hasDatingProfile = true
				)
			)
		}

		override suspend fun getUserCard(userId: String): ApiOperationResult<UserCard> =
			ApiOperationResult.ValidationError()

		override suspend fun getOwnDatingProfile(): ApiOperationResult<DatingProfile?> =
			ApiOperationResult.Success(null)

		override suspend fun upsertOwnDatingProfile(
			draft: DatingProfileDraft
		): ApiOperationResult<DatingProfile> = ApiOperationResult.ValidationError()

		override suspend fun uploadDatingProfilePhoto(
			contentType: String,
			bytes: ByteArray
		): ApiOperationResult<String> = ApiOperationResult.ValidationError()
	}

	private class FakeChatGateway : ChatGateway {
		override fun observeOwnChats(): Flow<List<ChatPreview>> = flowOf(emptyList())

		override suspend fun refreshOwnChats(): ApiOperationResult<Unit> = ApiOperationResult.Success(Unit)
	}

	private class FakeMatchGateway : MatchGateway {
		override suspend fun getIncomingLikes(): ApiOperationResult<List<IncomingLike>> =
			ApiOperationResult.Success(emptyList())

		override suspend fun getRecommendations(): ApiOperationResult<List<Recommendation>> =
			ApiOperationResult.Success(emptyList())

		override suspend fun getOwnTargeting(): ApiOperationResult<TargetingPreferences> =
			ApiOperationResult.Success(
				TargetingPreferences(
					region = null,
					genderTarget = TargetGender.Both,
					ageFrom = 18,
					ageTo = 30
				)
			)

		override suspend fun upsertOwnTargeting(
			draft: TargetingPreferencesDraft
		): ApiOperationResult<TargetingPreferences> = ApiOperationResult.Success(
			TargetingPreferences(
				region = draft.region,
				genderTarget = draft.genderTarget,
				ageFrom = draft.ageFrom,
				ageTo = draft.ageTo
			)
		)

		override suspend fun createInteraction(
			candidateUserId: String,
			action: InteractionAction,
			mlRecommendationId: String?
		): ApiOperationResult<Interaction> = ApiOperationResult.ValidationError()
	}

	private class FakePendingMatchActionStore : PendingMatchActionStore {
		private var actions: List<PendingMatchAction> = emptyList()

		override fun getAll(): List<PendingMatchAction> = actions

		override fun saveAll(actions: List<PendingMatchAction>) {
			this.actions = actions
		}

		override fun clear() {
			actions = emptyList()
		}
	}
}
