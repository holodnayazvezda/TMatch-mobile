package com.prod.matching.presentation

import com.prod.common.api.model.RecommendationReasonSummary
import com.prod.match.domain.model.IncomingLike
import com.prod.match.domain.model.Recommendation
import com.prod.match.domain.model.RecommendationCandidateProfile
import com.prod.match.domain.model.RecommendationReasonType
import com.prod.matching.model.MatchingDeckItem.Profile
import java.time.LocalDate
import java.time.Period

class RecommendationUiMapper {
	private val incomingLikeReason = RecommendationReasonSummary(
		label = "Вы понравились",
		description = "Этот пользователь уже лайкнул вас и ждёт вашего ответа."
	)

	fun mapIncomingLikes(items: List<IncomingLike>): List<Profile> {
		return items.map { incomingLike ->
			val profile = incomingLike.candidateCard?.profile
			Profile(
				id = incomingLike.likerUserId,
				name = profile.toDisplayName(
					fallback = "Пользователь ${incomingLike.likerUserId.take(8)}"
				),
				age = profile?.birthDate?.toAgeOrNull(),
				reasons = listOf(incomingLikeReason) + incomingLike.reasons.toReasonSummaries(),
				imageUrl = incomingLike.candidateCard?.datingProfile?.photos?.firstOrNull(),
				recommendationId = null,
				isIncomingLike = true
			)
		}
	}

	fun mapRecommendations(items: List<Recommendation>): List<Profile> {
		return items.map { recommendation ->
			val profile = recommendation.candidateCard?.profile
			Profile(
				id = recommendation.candidateUserId,
				name = profile.toDisplayName(
					fallback = "Пользователь ${recommendation.candidateUserId.take(8)}"
				),
				age = profile?.birthDate?.toAgeOrNull(),
				reasons = recommendation.reasons.toReasonSummaries().ifEmpty { listOf(defaultRecommendationReason) },
				imageUrl = recommendation.candidateCard?.datingProfile?.photos?.firstOrNull(),
				recommendationId = recommendation.mlRecommendationId,
				isIncomingLike = false
			)
		}
	}

	private val defaultRecommendationReason = RecommendationReasonSummary(
		label = "Есть потенциальное совпадение",
		description = "Система нашла общий паттерн поведения и показала профиль в рекомендациях."
	)

	private fun List<com.prod.match.domain.model.RecommendationReason>.toReasonSummaries(): List<RecommendationReasonSummary> {
		return map {
			it.reasonType.toUiReasonSummary()
		}.distinct()
	}

	private fun RecommendationReasonType.toUiReasonSummary(): RecommendationReasonSummary {
		val uiLabel = toUiLabel()
		val uiDescription = when (this) {
			RecommendationReasonType.ACTIVITY_FREQUENCY ->
				"У вас обоих часто совпадает общий ритм финансовой активности."
			RecommendationReasonType.UNIQUE_OPERATIONS ->
				"Вы часто пользуетесь разными финансовыми сценариями, а не чем-то одним."
			RecommendationReasonType.ACTIVE_DAILY ->
				"У вас обоих часто бывают активные дни в похожем ритме."
			RecommendationReasonType.ACTIVE_MONTHLY ->
				"У вас обоих часто сохраняется активность на протяжении месяца."
			RecommendationReasonType.ACTIVE_WEEKLY ->
				"У вас обоих часто бывают активные недели."
			RecommendationReasonType.FIRST_TXN ->
				"Вы оба давно пользуетесь финансовыми сервисами и накопили длинную историю операций."
			RecommendationReasonType.LAST_TXN ->
				"Вы оба были активны совсем недавно."
			RecommendationReasonType.ACTIVITY_DURATION ->
				"У вас обоих часто сохраняется стабильная активность на длинной дистанции."
			RecommendationReasonType.AVG_DAILY_ACTIVE ->
				"Вы часто совершаете много операций в те дни, когда особенно активны."
			RecommendationReasonType.AVG_MONTHLY_ACTIVE ->
				"Вы часто совершаете много операций в активные месяцы."
			RecommendationReasonType.AVG_WEEKLY_ACTIVE ->
				"Вы часто совершаете много операций в активные недели."
			RecommendationReasonType.PRIME_HOUR ->
				"Вы часто бываете активны примерно в одно и то же время дня."
			RecommendationReasonType.PRIME_WEEKDAY ->
				"Вы часто проявляете активность в один и тот же день недели."
			RecommendationReasonType.WEEKEND_ACTIVITY ->
				"Вы часто бываете активны по выходным."
			RecommendationReasonType.WEEKDAY_ACTIVITY ->
				"Вы часто бываете активны по будням."
			RecommendationReasonType.NIGHT_ACTIVITY ->
				"Вы часто проявляете активность ночью."
			RecommendationReasonType.MORNING_ACTIVITY ->
				"Вы часто бываете активны по утрам."
			RecommendationReasonType.AFTERNOON_ACTIVITY ->
				"Вы часто проявляете активность днем."
			RecommendationReasonType.EVENING_ACTIVITY ->
				"Вы часто бываете активны по вечерам."
			RecommendationReasonType.PRIME_DAYPART ->
				"Вы часто проявляете активность в одну и ту же часть дня."
			RecommendationReasonType.PEAK_DAILY ->
				"У вас обоих иногда бывают очень насыщенные по операциям дни."
			RecommendationReasonType.MEDIAN_DAILY_ACTIVE ->
				"У вас обоих часто бывает похожее обычное число операций за день."
			RecommendationReasonType.CATEGORY_DIVERSITY ->
				"У вас обоих часто похожий разброс трат по разным категориям."
			RecommendationReasonType.DOMINANT_CATEGORY ->
				"У вас обоих часто выделяется одна особенно важная категория трат."
			RecommendationReasonType.CATEGORY_PROFILE ->
				"У вас обоих часто похожий общий профиль трат по категориям."
			RecommendationReasonType.MCC_DIVERSITY ->
				"У вас обоих часто похожий набор типов мест, где вы что-то покупаете."
			RecommendationReasonType.DOMINANT_MCC ->
				"У вас обоих часто выделяется один и тот же тип мест для покупок."
			RecommendationReasonType.MCC_PROFILE ->
				"У вас обоих часто похожий профиль покупок по типам мест."
			RecommendationReasonType.MERCHANT_DIVERSITY ->
				"У вас обоих часто широкий круг продавцов, у которых вы совершаете покупки."
			RecommendationReasonType.FAVORITE_MERCHANT ->
				"Вы часто возвращаетесь к одним и тем же любимым продавцам."
			RecommendationReasonType.MERCHANT_DIVERSITY_SHARE ->
				"У вас обоих часто похоже распределяются покупки между разными продавцами."
			RecommendationReasonType.Unknown ->
				"Система нашла пересечение в вашем поведении и поэтому показала этого пользователя."
			else -> "У вас обоих часто встречаются траты в категории \"$uiLabel\"."
		}
		return RecommendationReasonSummary(
			label = uiLabel,
			description = uiDescription
		)
	}

	private fun RecommendationReasonType.toUiLabel(): String {
		return when (this) {
			RecommendationReasonType.UNIQUE_OPERATIONS -> "Разные сценарии"
			RecommendationReasonType.ACTIVE_DAILY -> "Активные дни"
			RecommendationReasonType.ACTIVE_WEEKLY -> "Активные недели"
			RecommendationReasonType.ACTIVE_MONTHLY -> "Активные месяцы"
			RecommendationReasonType.AVG_DAILY_ACTIVE -> "Насыщенные дни"
			RecommendationReasonType.AVG_WEEKLY_ACTIVE -> "Насыщенные недели"
			RecommendationReasonType.AVG_MONTHLY_ACTIVE -> "Насыщенные месяцы"
			RecommendationReasonType.PRIME_HOUR -> "Час активности"
			RecommendationReasonType.PRIME_WEEKDAY -> "День активности"
			RecommendationReasonType.PRIME_DAYPART -> "Часть дня"
			RecommendationReasonType.CATEGORY_PROFILE -> "Профиль трат"
			RecommendationReasonType.MCC_DIVERSITY -> "Разные места"
			RecommendationReasonType.DOMINANT_MCC -> "Любимый тип мест"
			RecommendationReasonType.MCC_PROFILE -> "Профиль мест"
			RecommendationReasonType.SPECIALTY_FOOD -> "Специализированные продукты"
			RecommendationReasonType.OTHER_RETAIL_SERVICES -> "Прочие покупки"
			RecommendationReasonType.LOCAL_TRANSPORT -> "Городской транспорт"
			RecommendationReasonType.WHOLESALE_CLUBS -> "Крупные магазины"
			RecommendationReasonType.DIRECT_MARKETING -> "Онлайн-заказы"
			RecommendationReasonType.COMPUTER_NETWORKS -> "Онлайн-сервисы"
			RecommendationReasonType.UNKNOWN_CATEGORY -> "Другие траты"
			RecommendationReasonType.OTHER_CATEGORIES -> "Прочие траты"
			RecommendationReasonType.UNKNOWN_MCC -> "Другие места"
			RecommendationReasonType.OTHER_MCC -> "Прочие места"
			else -> label
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
