package com.prod.profile.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.EntryProviderScope

fun EntryProviderScope<Any>.profileGraph() {
	entry<ProfileNavKey> {
		ProfileScreen()
	}
}

@Composable
private fun ProfileScreen() {
	Box(
		modifier = Modifier.fillMaxSize(),
		contentAlignment = Alignment.Center
	) {
		Text(
			text = "Профиль",
			style = MaterialTheme.typography.headlineMedium,
			color = MaterialTheme.colorScheme.onBackground
		)
	}
}
