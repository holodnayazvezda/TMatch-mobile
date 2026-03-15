package com.prod.matching.model

data class MatchProfile(
	val id: String,
	val name: String,
	val age: Int?,
	val matchLabel: String,
	val imageUrl: String?,
	val recommendationId: String? = null
)