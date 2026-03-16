package com.prod.matching.presentation

import com.prod.match.domain.model.IncomingLike
import com.prod.match.domain.model.Recommendation
import com.prod.match.domain.model.RecommendationCandidateCard
import com.prod.match.domain.model.RecommendationCandidateDatingProfile
import com.prod.match.domain.model.RecommendationCandidateDatingTrait
import com.prod.match.domain.model.RecommendationCandidateProfile
import com.prod.match.domain.model.RecommendationReason
import com.prod.match.domain.model.RecommendationReasonType
import org.junit.Assert.assertEquals
import org.junit.Test

class RecommendationUiMapperTest {

	@Test
	fun `maps incoming like reasons to visible labels and keeps liked me badge`() {
		val mapper = RecommendationUiMapper()

		val profiles = mapper.mapIncomingLikes(
			listOf(
				IncomingLike(
					likerUserId = "candidate-1",
					reasons = listOf(
						RecommendationReason(
							score = 0.9f,
							reasonType = RecommendationReasonType.RESTAURANTS
						),
						RecommendationReason(
							score = 0.8f,
							reasonType = RecommendationReasonType.EVENING_ACTIVITY
						)
					),
					candidateCard = RecommendationCandidateCard(
						userId = "candidate-1",
						profile = RecommendationCandidateProfile(
							firstName = "Анна",
							lastName = "Иванова",
							birthDate = "1998-01-01",
							region = null,
							avatarUrl = null
						),
						datingProfile = RecommendationCandidateDatingProfile(
							photos = listOf("https://example.com/photo.jpg"),
							traits = emptyList()
						)
					)
				)
			)
		)

		assertEquals(
			listOf("Вы понравились", "Рестораны", "Вечерняя активность"),
			profiles.single().reasons.map { it.label }
		)
	}

	@Test
	fun `maps all recommendation reasons to visible labels`() {
		val mapper = RecommendationUiMapper()

		val profiles = mapper.mapRecommendations(
			listOf(
				Recommendation(
					mlRecommendationId = "rec-1",
					userId = "user-1",
					candidateUserId = "candidate-1",
					reasons = listOf(
						RecommendationReason(
							score = 0.9f,
							reasonType = RecommendationReasonType.RESTAURANTS
						),
						RecommendationReason(
							score = 0.8f,
							reasonType = RecommendationReasonType.EVENING_ACTIVITY
						),
						RecommendationReason(
							score = 0.7f,
							reasonType = RecommendationReasonType.RESTAURANTS
						)
					),
					candidateCard = RecommendationCandidateCard(
						userId = "candidate-1",
						profile = RecommendationCandidateProfile(
							firstName = "Анна",
							lastName = "Иванова",
							birthDate = "1998-01-01",
							region = null,
							avatarUrl = null
						),
						datingProfile = RecommendationCandidateDatingProfile(
							photos = listOf("https://example.com/photo.jpg"),
							traits = listOf(
								RecommendationCandidateDatingTrait(
									traitCode = "kind",
									score = 0.8f,
									isHidden = false
								)
							)
						)
					)
				)
			)
		)

		assertEquals(
			listOf("Рестораны", "Вечерняя активность"),
			profiles.single().reasons.map { it.label }
		)
	}

	@Test
	fun `uses fallback label when reasons are empty`() {
		val mapper = RecommendationUiMapper()

		val profiles = mapper.mapRecommendations(
			listOf(
				Recommendation(
					mlRecommendationId = "rec-1",
					userId = "user-1",
					candidateUserId = "candidate-1",
					reasons = emptyList(),
					candidateCard = null
				)
			)
		)

		assertEquals(listOf("Есть потенциальное совпадение"), profiles.single().reasons.map { it.label })
	}
}
