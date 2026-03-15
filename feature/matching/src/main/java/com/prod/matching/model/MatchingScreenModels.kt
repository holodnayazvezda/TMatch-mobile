package com.prod.matching.model

import androidx.annotation.DrawableRes
import com.prod.matching.R

data class MatchProfile(
	val id: String,
	val name: String,
	val age: Int,
	val matchLabel: String,
	@param:DrawableRes val imageRes: Int
)

object MatchingContentController {
	private val PlaceholderImageRes = R.drawable.match_user_placeholder

	private val sampleProfiles = listOf(
		MatchProfile(
			id = "1",
			name = "Иван Васильевич",
			age = 67,
			matchLabel = "У вас схожий образ жизни",
			imageRes = PlaceholderImageRes
		),
		MatchProfile(
			id = "2",
			name = "Марина Орлова",
			age = 63,
			matchLabel = "Совпадают интересы в путешествиях",
			imageRes = PlaceholderImageRes
		),
		MatchProfile(
			id = "3",
			name = "Александр Белый",
			age = 70,
			matchLabel = "Похожие семейные ценности",
			imageRes = PlaceholderImageRes
		)
	)

	fun profiles(): List<MatchProfile> = sampleProfiles
}
