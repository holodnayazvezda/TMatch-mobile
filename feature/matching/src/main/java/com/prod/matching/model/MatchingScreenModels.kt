package com.prod.matching.model

data class MatchProfile(
	val id: String,
	val name: String,
	val age: Int?,
	val matchLabel: String,
	val imageUrl: String?,
	val recommendationId: String? = null
)

object MatchingContentController {
	private val sampleProfiles = listOf(
		MatchProfile(
			id = "1",
			name = "Иван Васильевич",
			age = 67,
			matchLabel = "У вас схожий образ жизни",
			imageUrl = null
		),
		MatchProfile(
			id = "2",
			name = "Марина Орлова",
			age = 63,
			matchLabel = "Совпадают интересы в путешествиях",
			imageUrl = null
		),
		MatchProfile(
			id = "3",
			name = "Александр Белый",
			age = 70,
			matchLabel = "Похожие семейные ценности",
			imageUrl = null
		)
	)

	fun profiles(): List<MatchProfile> = sampleProfiles
}
