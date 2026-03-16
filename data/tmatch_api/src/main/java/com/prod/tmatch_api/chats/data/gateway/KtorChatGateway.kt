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
import com.prod.tmatch_api.chats.data.local.ChatEntity
import com.prod.tmatch_api.chats.data.local.ChatPreviewDao
import com.prod.tmatch_api.chats.data.local.ChatUserEntity
import com.prod.tmatch_api.chats.data.local.ChatWithUserEntity
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
					chats = items.map { it.toChatEntity(ownerUserId = session.userId) },
					users = items.mapNotNull { it.toChatUserEntity(ownerUserId = session.userId) }
						.distinctBy { it.ownerUserId to it.userId }
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

	private fun ChatItemDto.toChatEntity(ownerUserId: String): ChatEntity {
		return ChatEntity(
			ownerUserId = ownerUserId,
			chatId = chatId,
			otherUserId = otherUserId,
			lastMessageId = lastMessageId,
			lastMessageText = lastMessageText,
			lastMessageCreatedAt = lastMessageCreatedAt
		)
	}

	private fun ChatItemDto.toChatUserEntity(ownerUserId: String): ChatUserEntity? {
		return otherUserCard?.let { card ->
			ChatUserEntity(
				ownerUserId = ownerUserId,
				userId = otherUserId,
				firstName = card.profile?.firstName,
				lastName = card.profile?.lastName,
				birthDate = card.profile?.birthDate,
				gender = card.profile?.gender,
				region = card.profile?.region,
				avatarUrl = card.profile?.avatarUrl,
				datingPhotos = card.datingProfile?.photos.orEmpty().joinToString("|")
			)
		}
	}

	private fun ChatWithUserEntity.toDomain(): ChatPreview {
		val chat = chat
		val user = user
		val profile = if (
			user?.firstName != null &&
			user.birthDate != null &&
			user.gender != null
		) {
			UserCardProfile(
				firstName = user.firstName,
				lastName = user.lastName,
				birthDate = user.birthDate,
				gender = user.gender,
				region = user.region,
				avatarUrl = user.avatarUrl
			)
		} else {
			null
		}
		val photos = user?.datingPhotos
			?.takeIf { it.isNotBlank() }
			?.split('|')
			?.filter { it.isNotBlank() }
			.orEmpty()

		return ChatPreview(
			chatId = chat.chatId,
			otherUserId = chat.otherUserId,
			lastMessageId = chat.lastMessageId,
			lastMessageText = chat.lastMessageText,
			lastMessageCreatedAt = chat.lastMessageCreatedAt,
			otherUserCard = if (profile != null || photos.isNotEmpty()) {
				UserCard(
					userId = chat.otherUserId,
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
