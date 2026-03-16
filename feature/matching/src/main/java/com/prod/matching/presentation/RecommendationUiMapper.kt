package com.prod.matching.presentation

import com.prod.common.api.model.RecommendationReasonSummary
import com.prod.match.domain.model.IncomingLike
import com.prod.match.domain.model.Recommendation
import com.prod.match.domain.model.RecommendationCandidateProfile
import com.prod.matching.model.MatchProfile
import java.time.LocalDate
import java.time.Period

class RecommendationUiMapper {
	private val incomingLikeReason = RecommendationReasonSummary(
		label = "Вы понравились",
		description = "Этот пользователь уже лайкнул вас и ждёт вашего ответа."
	)

	fun mapIncomingLikes(items: List<IncomingLike>): List<MatchProfile> {
		return items.map { incomingLike ->
			val profile = incomingLike.candidateCard?.profile
			MatchProfile(
				id = incomingLike.likerUserId,
				name = profile.toDisplayName(
					fallback = "Пользователь ${incomingLike.likerUserId.take(8)}"
				),
				age = profile?.birthDate?.toAgeOrNull(),
				reasons = listOf(incomingLikeReason),
				imageUrl = incomingLike.candidateCard?.datingProfile?.photos?.firstOrNull(),
				recommendationId = null,
				isIncomingLike = true
			)
		}
	}

	fun mapRecommendations(items: List<Recommendation>): List<MatchProfile> {
		return items.map { recommendation ->
			val profile = recommendation.candidateCard?.profile
			MatchProfile(
				id = recommendation.candidateUserId,
				name = profile.toDisplayName(
					fallback = "Пользователь ${recommendation.candidateUserId.take(8)}"
				),
				age = profile?.birthDate?.toAgeOrNull(),
				reasons = recommendation.reasons
					.map {
						RecommendationReasonSummary(
							label = it.reasonType.label,
							description = it.reasonType.description
						)
					}
					.distinct()
					.ifEmpty {
						listOf(
							RecommendationReasonSummary(
								label = "Есть потенциальное совпадение",
								description = "Система нашла общий паттерн поведения и показала профиль в рекомендациях."
							)
						)
					},
				imageUrl = recommendation.candidateCard?.datingProfile?.photos?.firstOrNull(),
				recommendationId = recommendation.mlRecommendationId,
				isIncomingLike = false
			)
		}
	}

	private fun RecommendationCandidateProfile?.toDisplayName(
		fallback: String
	): String {
		if (this == null) return fallback
		return listOf(firstName, lastName)
			.filterNot { it.isNullOrBlank() }
			.joinToString(" ")
			.ifBlank { fallback }
	}

	private fun String.toAgeOrNull(): Int? {
		val birthDate = runCatching { LocalDate.parse(this) }.getOrNull() ?: return null
		return Period.between(birthDate, LocalDate.now()).years.takeIf { it >= 0 }
	}
}
