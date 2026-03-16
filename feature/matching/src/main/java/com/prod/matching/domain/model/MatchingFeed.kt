package com.prod.matching.domain.model

import com.prod.match.domain.model.IncomingLike
import com.prod.match.domain.model.Recommendation

data class MatchingFeed(
	val incomingLikes: List<IncomingLike>,
	val recommendations: List<Recommendation>
)
