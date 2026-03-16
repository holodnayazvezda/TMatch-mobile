package com.prod.matching.domain.interactor

import com.prod.matching.model.MatchingDeckItem
import com.prod.matching.model.MatchingDeckItem.Profile

class MatchingDeckCoordinator {
	private var incomingLikeProfiles: List<Profile> = emptyList()
	private val recommendationQueue = ArrayDeque<Profile>()

	fun replaceFeed(
		incomingLikes: List<Profile>,
		recommendations: List<Profile>
	): List<MatchingDeckItem> {
		incomingLikeProfiles = incomingLikes
		recommendationQueue.clear()
		recommendationQueue.addAll(
			recommendations.filterNot { it.id in incomingLikes.mapTo(hashSetOf(), Profile::id) }
		)
		return snapshot()
	}

	fun appendRecommendations(recommendations: List<Profile>): List<MatchingDeckItem> {
		if (recommendations.isEmpty()) {
			return snapshot()
		}

		recommendationQueue.addAll(recommendations.filterNot(::isKnownProfile))
		return snapshot()
	}

	fun removeProfile(
		profileId: String,
		preferredStartIndex: Int
	): DeckRemovalResult {
		incomingLikeProfiles = incomingLikeProfiles.filterNot { it.id == profileId }
		recommendationQueue.removeAll { it.id == profileId }
		val items = snapshot()
		return DeckRemovalResult(
			items = items,
			nextProfileIndex = items.findNextProfileIndex(preferredStartIndex)
		)
	}

	fun clear(): List<MatchingDeckItem> {
		incomingLikeProfiles = emptyList()
		recommendationQueue.clear()
		return emptyList()
	}

	private fun snapshot(): List<MatchingDeckItem> {
		val recommendationProfiles = recommendationQueue.toList()
		if (incomingLikeProfiles.isEmpty()) {
			return recommendationProfiles
		}
		if (recommendationProfiles.isEmpty()) {
			return incomingLikeProfiles
		}
		return incomingLikeProfiles + MatchingDeckItem.Transition() + recommendationProfiles
	}

	private fun isKnownProfile(profile: Profile): Boolean {
		val knownUserIds = incomingLikeProfiles.mapTo(hashSetOf(), Profile::id)
			.apply { addAll(recommendationQueue.map(Profile::id)) }
		return profile.id in knownUserIds
	}

	private fun List<MatchingDeckItem>.findNextProfileIndex(preferredStartIndex: Int): Int {
		if (isEmpty()) {
			return 0
		}

		for (index in preferredStartIndex.coerceAtLeast(0)..lastIndex) {
			if (this[index] is Profile) {
				return index
			}
		}

		for (index in lastIndex downTo 0) {
			if (this[index] is Profile) {
				return index
			}
		}

		return 0
	}
}

data class DeckRemovalResult(
	val items: List<MatchingDeckItem>,
	val nextProfileIndex: Int
)
