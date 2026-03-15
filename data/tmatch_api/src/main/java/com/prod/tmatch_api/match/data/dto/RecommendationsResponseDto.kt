package com.prod.tmatch_api.match.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class RecommendationsResponseDto(
	val items: List<RecommendationDto>
)
