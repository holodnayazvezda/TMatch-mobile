package com.prod.match.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data class MatchingNavKey(
	val source: String = "main_tabs"
) : NavKey
