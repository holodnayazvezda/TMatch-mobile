package com.prod.matching.presentation

import com.prod.match.domain.model.Recommendation
import com.prod.match.domain.model.RecommendationCandidateProfile
import com.prod.match.domain.model.RecommendationReason
import com.prod.match.domain.model.RecommendationReasonType
import com.prod.matching.R
import com.prod.matching.model.MatchProfile
import java.time.LocalDate
import java.time.Period

class RecommendationUiMapper {
	fun mapRecommendations(items: List<Recommendation>): List<MatchProfile> {
		return items.map { recommendation ->
			val profile = recommendation.candidateCard?.profile
			MatchProfile(
				id = recommendation.candidateUserId,
				name = profile.toDisplayName(
					fallback = "Пользователь ${recommendation.candidateUserId.take(8)}"
				),
				age = profile?.birthDate?.toAgeOrNull(),
				matchLabel = recommendation.reasons.maxByOrNull { it.score }?.toLabel()
					?: "Есть потенциальное совпадение",
				imageUrl = recommendation.candidateCard?.datingProfile?.photos?.firstOrNull(),
				recommendationId = recommendation.mlRecommendationId
			)
		}
	}

	private fun RecommendationReason.toLabel(): String {
		return when (reasonType) {
			RecommendationReasonType.Lifestyle -> "Схожий образ жизни"
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
