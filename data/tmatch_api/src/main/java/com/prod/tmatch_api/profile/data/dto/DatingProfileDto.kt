package com.prod.tmatch_api.profile.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DatingProfileDto(
	@SerialName("user_id")
	val userId: String,
	val photos: List<String>
)
