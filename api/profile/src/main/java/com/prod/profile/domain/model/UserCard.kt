package com.prod.profile.domain.model

data class UserCard(
	val userId: String,
	val profile: UserCardProfile?,
	val datingProfile: UserCardDatingProfile?
)

data class UserCardProfile(
	val firstName: String,
	val lastName: String?,
	val birthDate: String,
	val gender: String,
	val region: String?,
	val avatarUrl: String?
)

data class UserCardDatingProfile(
	val photos: List<String>,
	val traits: List<UserCardDatingTrait>
)

data class UserCardDatingTrait(
	val traitCode: String,
	val score: Float,
	val isHidden: Boolean
)
