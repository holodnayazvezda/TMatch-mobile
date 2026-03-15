package com.prod.profile.navigation

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.EntryProviderScope
import com.prod.ui.components.buttons.AppBigButton
import com.prod.ui.components.profile.ProfileImageCropDialog
import com.prod.ui.components.profile.SelectableProfileImage
import com.prod.ui.components.profile.ProfileImageStyle
import com.prod.ui.components.profile.loadBitmapFromUri
import com.prod.ui.components.profile.toPngByteArray
import org.koin.androidx.compose.koinViewModel

fun EntryProviderScope<Any>.profileGraph(
	onUnauthorized: () -> Unit,
	onRequestNoInternet: (retryAction: () -> Unit) -> Unit
) {
	entry<ProfileNavKey> {
		ProfileScreen(
			onUnauthorized = onUnauthorized,
			onRequestNoInternet = onRequestNoInternet
		)
	}
}

@Composable
private fun ProfileScreen(
	onUnauthorized: () -> Unit,
	onRequestNoInternet: (retryAction: () -> Unit) -> Unit,
	viewModel: com.prod.profile.presentation.ProfileViewModel = koinViewModel()
) {
	val uiState = viewModel.uiState
	val context = LocalContext.current
	var photoBitmap by remember { mutableStateOf<Bitmap?>(null) }
	var cropSourceBitmap by remember { mutableStateOf<Bitmap?>(null) }

	val pickerLauncher = rememberLauncherForActivityResult(
		contract = ActivityResultContracts.PickVisualMedia()
	) { uri ->
		cropSourceBitmap = uri?.let { context.loadBitmapFromUri(it) }
	}

	LaunchedEffect(Unit) {
		viewModel.initialize(
			onUnauthorized = onUnauthorized,
			onNoInternet = onRequestNoInternet
		)
	}

	Box(
		modifier = Modifier
			.fillMaxSize()
			.safeDrawingPadding()
	) {
		when {
			uiState.isLoading -> {
				CircularProgressIndicator(
					modifier = Modifier.align(Alignment.Center),
					color = MaterialTheme.colorScheme.primary
				)
			}

			uiState.requiresDatingPhoto -> {
				Column(
					modifier = Modifier
						.align(Alignment.Center)
						.fillMaxWidth()
						.padding(24.dp),
					verticalArrangement = Arrangement.spacedBy(16.dp),
					horizontalAlignment = Alignment.CenterHorizontally
				) {
					Text(
						text = "Загрузите фото для dating profile",
						style = MaterialTheme.typography.headlineSmall,
						color = MaterialTheme.colorScheme.onBackground
					)
					SelectableProfileImage(
						imageBitmap = photoBitmap,
						placeholderText = "Фото",
						contentDescription = "Dating profile photo",
						style = ProfileImageStyle.DatingPhoto,
						onClick = {
							pickerLauncher.launch(
								PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
							)
						},
						modifier = Modifier.fillMaxWidth()
					)
					uiState.error?.let { error ->
						Surface(
							modifier = Modifier.fillMaxWidth(),
							color = MaterialTheme.colorScheme.errorContainer,
							contentColor = MaterialTheme.colorScheme.onErrorContainer,
							shape = RoundedCornerShape(18.dp)
						) {
							Text(
								text = error,
								style = MaterialTheme.typography.bodyMedium,
								modifier = Modifier.padding(16.dp)
							)
						}
					}
					AppBigButton(
						buttonText = "Создать dating profile",
						isLoading = uiState.isSubmitting,
						onClick = {
							val bitmap = photoBitmap ?: run {
								viewModel.onPhotoRequired()
								return@AppBigButton
							}
							viewModel.createDatingProfile(
								imageBytes = bitmap.toPngByteArray(),
								onUnauthorized = onUnauthorized,
								onNoInternet = onRequestNoInternet
							)
						}
					)
				}
			}

			uiState.error != null -> {
				Column(
					modifier = Modifier
						.align(Alignment.Center)
						.fillMaxWidth()
						.padding(24.dp),
					verticalArrangement = Arrangement.spacedBy(16.dp),
					horizontalAlignment = Alignment.CenterHorizontally
				) {
					Surface(
						modifier = Modifier.fillMaxWidth(),
						color = MaterialTheme.colorScheme.errorContainer,
						contentColor = MaterialTheme.colorScheme.onErrorContainer,
						shape = RoundedCornerShape(18.dp)
					) {
						Text(
							text = uiState.error,
							style = MaterialTheme.typography.bodyMedium,
							modifier = Modifier.padding(16.dp)
						)
					}
					AppBigButton(
						buttonText = "Повторить",
						isLoading = false,
						onClick = {
							viewModel.retry(
								onUnauthorized = onUnauthorized,
								onNoInternet = onRequestNoInternet
							)
						}
					)
				}
			}

			else -> {
				Text(
					text = uiState.message ?: "Профиль",
					style = MaterialTheme.typography.headlineMedium,
					color = MaterialTheme.colorScheme.onBackground,
					modifier = Modifier.align(Alignment.Center)
				)
			}
		}
	}

	cropSourceBitmap?.let { sourceBitmap ->
		ProfileImageCropDialog(
			bitmap = sourceBitmap,
			title = "Обрежьте фото",
			cancelText = "Отмена",
			confirmText = "Применить",
			style = ProfileImageStyle.DatingPhoto,
			onDismiss = { cropSourceBitmap = null },
			onConfirm = { croppedBitmap ->
				photoBitmap = croppedBitmap
				cropSourceBitmap = null
			}
		)
	}
}
