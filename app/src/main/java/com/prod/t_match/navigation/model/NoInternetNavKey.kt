package com.prod.t_match.navigation.model

import kotlinx.serialization.Serializable

@Serializable
data class NoInternetNavKey(
	val requestId: Long
) : AppNavKey
