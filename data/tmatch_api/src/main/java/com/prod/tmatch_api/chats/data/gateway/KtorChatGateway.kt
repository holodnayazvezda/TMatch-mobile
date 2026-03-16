package com.prod.tmatch_api.chats.data.gateway

import com.prod.auth.domain.gateway.AuthSessionStore
import com.prod.chats.domain.gateway.ChatGateway
import com.prod.chats.domain.model.ChatPreview
import com.prod.common.api.model.ApiOperationResult
import com.prod.profile.domain.model.UserCard
import com.prod.profile.domain.model.UserCardDatingProfile
import com.prod.profile.domain.model.UserCardDatingTrait
import com.prod.profile.domain.model.UserCardProfile
import com.prod.tmatch_api.auth.data.dto.ApiErrorDto
import com.prod.tmatch_api.chats.data.dto.ChatItemDto
import com.prod.tmatch_api.chats.data.dto.ChatsResponseDto
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

class KtorChatGateway(
	private val httpClient: HttpClient,
	authSessionStore: AuthSessionStore
) : ChatGateway {

	private val gatewaySupport = KtorGatewaySupport(authSessionStore)

	override suspend fun getOwnChats(): ApiOperationResult<List<ChatPreview>> {
		return try {
			val response = httpClient.get("/chats/me") {
				accept(ContentType.Application.Json)
				with(gatewaySupport) {
					applyAuthorizedRequest()
				}
			}

			if (response.status.isSuccess()) {
				ApiOperationResult.Success(
					response.body<ChatsResponseDto>().items.map { it.toDomain() }
				)
			} else {
				gatewaySupport.mapErrorResponse(
					status = response.status,
					responseBody = runCatching { response.body<ApiErrorDto>() }.getOrNull()
				)
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
