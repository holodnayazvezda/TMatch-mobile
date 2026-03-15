package com.prod.tmatch_api.match.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class RecommendationCandidateDatingProfileDto(
	val photos: List<String> = emptyList(),
	val traits: List<RecommendationCandidateDatingTraitDto> = emptyList()
)
