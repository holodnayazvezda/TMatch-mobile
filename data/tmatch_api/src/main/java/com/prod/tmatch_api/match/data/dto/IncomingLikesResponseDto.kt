package com.prod.tmatch_api.match.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class IncomingLikesResponseDto(
	val items: List<IncomingLikeItemDto>
)
