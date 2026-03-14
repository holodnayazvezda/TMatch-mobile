package com.prod.t_match.navigation.model

import kotlinx.serialization.Serializable

@Serializable
data class BlankNavKey(
	val sessionStartedAtMillis: Long
) : AppNavKey
