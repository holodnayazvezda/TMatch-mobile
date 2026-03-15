package com.prod.tmatch_api.auth.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UploadDatingPhotoResponseDto(
	@SerialName("photo_url")
	val photoUrl: String
)
