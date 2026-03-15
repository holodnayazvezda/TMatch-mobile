package com.prod.tmatch_api.match.data.gateway

import com.prod.auth.domain.gateway.AuthSessionStore
import com.prod.common.api.model.ApiOperationResult
import com.prod.match.domain.gateway.MatchGateway
import com.prod.match.domain.model.Interaction
import com.prod.match.domain.model.InteractionAction
import com.prod.match.domain.model.Recommendation
import com.prod.match.domain.model.RecommendationCandidateCard
import com.prod.match.domain.model.RecommendationCandidateDatingProfile
import com.prod.match.domain.model.RecommendationCandidateProfile
import com.prod.match.domain.model.RecommendationReason
import com.prod.match.domain.model.RecommendationReasonType
import com.prod.tmatch_api.auth.data.dto.ApiErrorDto
import com.prod.tmatch_api.common.data.network.KtorGatewaySupport
import com.prod.tmatch_api.match.data.dto.CreateInteractionRequestDto
import com.prod.tmatch_api.match.data.dto.InteractionResponseDto
import com.prod.tmatch_api.match.data.dto.RecommendationCandidateCardDto
import com.prod.tmatch_api.match.data.dto.RecommendationCandidateDatingProfileDto
import com.prod.tmatch_api.match.data.dto.RecommendationCandidateProfileDto
import com.prod.tmatch_api.match.data.dto.RecommendationDto
import com.prod.tmatch_api.match.data.dto.RecommendationsResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess

class KtorMatchGateway(
	private val httpClient: HttpClient,
	authSessionStore: AuthSessionStore
) : MatchGateway {

	private val gatewaySupport = KtorGatewaySupport(authSessionStore)

	override suspend fun getRecommendations(): ApiOperationResult<List<Recommendation>> {
		return try {
			val response = httpClient.get("/recommendations") {
				accept(ContentType.Application.Json)
				with(gatewaySupport) {
					applyAuthorizedRequest()
				}
			}

			if (response.status.isSuccess()) {
				ApiOperationResult.Success(
					response.body<RecommendationsResponseDto>().items.map { it.toDomain() }
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

	override suspend fun createInteraction(
		candidateUserId: String,
		action: InteractionAction,
		mlRecommendationId: String?
	): ApiOperationResult<Interaction> {
		return try {
			val response = httpClient.post("/interactions") {
				accept(ContentType.Application.Json)
				header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
				with(gatewaySupport) {
					applyAuthorizedRequest()
				}
				setBody(
					CreateInteractionRequestDto(
						candidateUserId = candidateUserId,
						action = action.toApiValue(),
						mlRecommendationId = mlRecommendationId
					)
				)
			}

			if (response.status.isSuccess()) {
				ApiOperationResult.Success(response.body<InteractionResponseDto>().toDomain())
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

	private fun RecommendationDto.toDomain(): Recommendation {
		return Recommendation(
			mlRecommendationId = mlRecommendationId,
			userId = userId,
			candidateUserId = candidateUserId,
			reasons = reasons.map { (featureName, score) ->
				RecommendationReason(
					score = score,
					reasonType = featureName.toRecommendationReasonType()
				)
			},
			candidateCard = candidateCard?.toDomain()
		)
	}

	private fun RecommendationCandidateCardDto.toDomain(): RecommendationCandidateCard {
		return RecommendationCandidateCard(
			userId = userId,
			profile = profile?.toDomain(),
			datingProfile = datingProfile?.toDomain()
		)
	}

	private fun RecommendationCandidateProfileDto.toDomain(): RecommendationCandidateProfile {
		return RecommendationCandidateProfile(
			firstName = firstName,
			lastName = lastName,
			birthDate = birthDate,
			region = region,
			avatarUrl = avatarUrl
		)
	}

	private fun RecommendationCandidateDatingProfileDto.toDomain(): RecommendationCandidateDatingProfile {
		return RecommendationCandidateDatingProfile(
			photos = photos
		)
	}

	private fun String.toRecommendationReasonType(): RecommendationReasonType {
		return when (this) {
			"lifestyle" -> RecommendationReasonType.Lifestyle
			else -> error("Unsupported recommendation reason type: $this")
		}
	}

	private fun InteractionResponseDto.toDomain(): Interaction {
		return Interaction(
			interactionId = interactionId,
			actorUserId = actorUserId,
			candidateUserId = candidateUserId,
			action = action.toDomainAction(),
			createdAt = createdAt,
			mlRecommendationId = mlRecommendationId
		)
	}

	private fun InteractionAction.toApiValue(): String {
		return when (this) {
			InteractionAction.Like -> "like"
			InteractionAction.Dislike -> "dislike"
		}
	}

	private fun String.toDomainAction(): InteractionAction {
		return when (this) {
			"dislike" -> InteractionAction.Dislike
			"like" -> InteractionAction.Like
			else -> error("Unsupported interaction action: $this")
		}
	}
}
