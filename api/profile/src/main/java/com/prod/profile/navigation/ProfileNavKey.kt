package com.prod.profile.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data class ProfileNavKey(
	val source: String = "main_tabs"
) : NavKey
