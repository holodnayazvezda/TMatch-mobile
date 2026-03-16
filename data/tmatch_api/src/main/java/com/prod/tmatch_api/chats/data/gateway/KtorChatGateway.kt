package com.prod.tmatch_api.chats.data.gateway

import com.prod.auth.domain.gateway.AuthSessionStore
import com.prod.chats.domain.gateway.ChatGateway
import com.prod.chats.domain.model.ChatPreview
import com.prod.common.api.model.ApiErrorReason
import com.prod.common.api.model.ApiOperationResult
import com.prod.profile.domain.model.UserCard
import com.prod.profile.domain.model.UserCardDatingProfile
import com.prod.profile.domain.model.UserCardDatingTrait
import com.prod.profile.domain.model.UserCardProfile
import com.prod.tmatch_api.auth.data.dto.ApiErrorDto
import com.prod.tmatch_api.chats.data.dto.ChatItemDto
import com.prod.tmatch_api.chats.data.dto.ChatsResponseDto
import com.prod.tmatch_api.chats.data.local.ChatPreviewDao
import com.prod.tmatch_api.chats.data.local.ChatPreviewEntity
import com.prod.tmatch_api.common.data.network.KtorGatewaySupport
import com.prod.tmatch_api.profile.data.dto.CardDatingProfileDto
import com.prod.tmatch_api.profile.data.dto.CardDatingTraitDto
import com.prod.tmatch_api.profile.data.dto.CardProfileDto
import com.prod.tmatch_api.profile.data.dto.OwnCardDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.http.ContentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

class KtorChatGateway(
	private val httpClient: HttpClient,
	private val authSessionStore: AuthSessionStore,
	private val chatPreviewDao: ChatPreviewDao
) : ChatGateway {

	private val gatewaySupport = KtorGatewaySupport(authSessionStore)

	override fun observeOwnChats(): Flow<List<ChatPreview>> {
		val ownerUserId = authSessionStore.get()?.userId ?: return flowOf(emptyList())
		return chatPreviewDao.observeChats(ownerUserId).map { items ->
			items.map { it.toDomain() }
		}
	}

	override suspend fun refreshOwnChats(): ApiOperationResult<Unit> {
		val session = authSessionStore.get()
			?: return ApiOperationResult.ApiError(ApiErrorReason.Unauthorized)

		return try {
			val response = httpClient.get("/chats/me") {
				accept(ContentType.Application.Json)
				with(gatewaySupport) {
					applyAuthorizedRequest()
				}
			}

			if (response.status.isSuccess()) {
				val items = response.body<ChatsResponseDto>().items
				chatPreviewDao.replaceChats(
					ownerUserId = session.userId,
					items = items.map { it.toEntity(ownerUserId = session.userId) }
				)
				ApiOperationResult.Success(Unit)
			} else {
				val error = gatewaySupport.mapErrorResponse(
					status = response.status,
					responseBody = runCatching { response.body<ApiErrorDto>() }.getOrNull()
				)
				if (
					error is ApiOperationResult.ApiError &&
					error.reason == ApiErrorReason.Unauthorized
				) {
					chatPreviewDao.deleteByOwnerUserId(session.userId)
				}
				error
			}
		} catch (exception: Exception) {
			gatewaySupport.handleException(exception)
		}
	}

	private fun ChatItemDto.toDomain(): ChatPreview {
		return ChatPreview(
			chatId = chatId,
			otherUserId = otherUserId,
			lastMessageId = lastMessageId,
			lastMessageText = lastMessageText,
			lastMessageCreatedAt = lastMessageCreatedAt,
			otherUserCard = otherUserCard?.toUserCard()
		)
	}

	private fun ChatItemDto.toEntity(ownerUserId: String): ChatPreviewEntity {
		return ChatPreviewEntity(
			ownerUserId = ownerUserId,
			chatId = chatId,
			otherUserId = otherUserId,
			lastMessageId = lastMessageId,
			lastMessageText = lastMessageText,
			lastMessageCreatedAt = lastMessageCreatedAt,
			otherProfileFirstName = otherUserCard?.profile?.firstName,
			otherProfileLastName = otherUserCard?.profile?.lastName,
			otherProfileBirthDate = otherUserCard?.profile?.birthDate,
			otherProfileGender = otherUserCard?.profile?.gender,
			otherProfileRegion = otherUserCard?.profile?.region,
			otherProfileAvatarUrl = otherUserCard?.profile?.avatarUrl,
			otherDatingPhotos = otherUserCard?.datingProfile?.photos.orEmpty().joinToString("|")
		)
	}

	private fun ChatPreviewEntity.toDomain(): ChatPreview {
		val profile = if (
			otherProfileFirstName != null &&
			otherProfileBirthDate != null &&
			otherProfileGender != null
		) {
			UserCardProfile(
				firstName = otherProfileFirstName,
				lastName = otherProfileLastName,
				birthDate = otherProfileBirthDate,
				gender = otherProfileGender,
				region = otherProfileRegion,
				avatarUrl = otherProfileAvatarUrl
			)
		} else {
			null
		}
		val photos = otherDatingPhotos
			.takeIf { it.isNotBlank() }
			?.split('|')
			?.filter { it.isNotBlank() }
			.orEmpty()

		return ChatPreview(
			chatId = chatId,
			otherUserId = otherUserId,
			lastMessageId = lastMessageId,
			lastMessageText = lastMessageText,
			lastMessageCreatedAt = lastMessageCreatedAt,
			otherUserCard = if (profile != null || photos.isNotEmpty()) {
				UserCard(
					userId = otherUserId,
					profile = profile,
					datingProfile = if (photos.isNotEmpty()) {
						UserCardDatingProfile(
							photos = photos,
							traits = emptyList()
						)
					} else {
						null
					}
				)
			} else {
				null
			}
		)
	}

	private fun OwnCardDto.toUserCard(): UserCard {
		return UserCard(
			userId = userId,
			profile = profile?.toDomain(),
			datingProfile = datingProfile?.toDomain()
		)
	}

	private fun CardProfileDto.toDomain(): UserCardProfile {
		return UserCardProfile(
			firstName = firstName,
			lastName = lastName,
			birthDate = birthDate,
			gender = gender,
			region = region,
			avatarUrl = avatarUrl
		)
	}

	private fun CardDatingProfileDto.toDomain(): UserCardDatingProfile {
		return UserCardDatingProfile(
			photos = photos,
			traits = traits.map { it.toDomain() }
		)
	}

	private fun CardDatingTraitDto.toDomain(): UserCardDatingTrait {
		return UserCardDatingTrait(
			traitCode = traitCode,
			score = score.toFloat(),
			isHidden = isHidden
		)
	}
}
