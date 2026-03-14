package com.prod.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data class NoInternetNavKey(
	val requestId: Long
) : NavKey
