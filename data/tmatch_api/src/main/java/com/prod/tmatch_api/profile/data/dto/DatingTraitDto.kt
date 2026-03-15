package com.prod.tmatch_api.profile.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DatingTraitDto(
	@SerialName("trait_code")
	val traitCode: String,
	val score: Float,
	@SerialName("is_hidden")
	val isHidden: Boolean
)
