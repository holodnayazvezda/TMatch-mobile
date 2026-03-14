package com.prod.chats.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data class ChatsNavKey(
	val source: String = "main_tabs"
) : NavKey
