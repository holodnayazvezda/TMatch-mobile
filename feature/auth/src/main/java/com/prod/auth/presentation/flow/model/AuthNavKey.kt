package com.prod.auth.presentation.flow.model

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface AuthNavKey : NavKey
