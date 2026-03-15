package com.prod.tmatch_api.profile.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class UpsertDatingProfileRequestDto(
	val photos: List<String>
)
