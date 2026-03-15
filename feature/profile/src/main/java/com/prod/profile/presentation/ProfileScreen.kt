package com.prod.profile.presentation

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.prod.core.ui.theme.LocalDimensions
import com.prod.core.ui.theme.LocalShapes
import com.prod.profile.R
import com.prod.ui.components.buttons.AppMediumButton
import com.prod.ui.components.profile.ProfileImageCropDialog
import com.prod.ui.components.profile.ProfileImageStyle
import com.prod.ui.components.profile.loadBitmapForCroppingFromUri
import com.prod.ui.components.profile.toPngByteArray
import com.prod.ui.theme.LocalTmatchExtraColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.androidx.compose.koinViewModel
import java.net.URL

@Composable
fun ProfileScreen(
	onUnauthorized: () -> Unit,
	onRequestNoInternet: (retryAction: () -> Unit) -> Unit,
	modifier: Modifier = Modifier,
	viewModel: ProfileViewModel = koinViewModel()
) {
	val uiState = viewModel.uiState
	val context = LocalContext.current
	var cropSourceBitmap by remember { mutableStateOf<Bitmap?>(null) }
	var uploadPreviewBitmap by remember { mutableStateOf<Bitmap?>(null) }

	val pickerLauncher = rememberLauncherForActivityResult(
		contract = ActivityResultContracts.PickVisualMedia()
	) { uri ->
		cropSourceBitmap = uri?.let { context.loadBitmapForCroppingFromUri(it) }
	}

	LaunchedEffect(Unit) {
		viewModel.initialize(
			onUnauthorized = onUnauthorized,
			onNoInternet = onRequestNoInternet
		)
	}

	LaunchedEffect(uiState.isSubmitting, uiState.error) {
		if (!uiState.isSubmitting && uiState.error != null) {
			uploadPreviewBitmap = null
		}
	}

	Box(
		modifier = modifier
			.fillMaxSize()
			.safeDrawingPadding()
	) {
		when {
			uiState.profileCard != null -> {
				ProfileCardScene(
					profileCard = uiState.profileCard,
					previewBitmap = uploadPreviewBitmap,
					isSubmitting = uiState.isSubmitting,
					error = uiState.error,
					onChangePhotoClick = {
						pickerLauncher.launch(
							PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
						)
					},
					modifier = Modifier.fillMaxSize()
				)
			}

			uiState.isLoading -> {
				CircularProgressIndicator(
					modifier = Modifier.align(Alignment.Center),
					color = MaterialTheme.colorScheme.primary
				)
			}

			uiState.error != null -> {
				ProfileLoadingError(
					message = uiState.error,
					onRetry = {
						viewModel.retry(
							onUnauthorized = onUnauthorized,
							onNoInternet = onRequestNoInternet
						)
					},
					modifier = Modifier
						.align(Alignment.Center)
						.padding(horizontal = 24.dp)
				)
			}
		}
	}

	cropSourceBitmap?.let { sourceBitmap ->
		ProfileImageCropDialog(
			bitmap = sourceBitmap,
			title = stringResource(R.string.profile_screen_crop_title),
			cancelText = stringResource(R.string.profile_screen_crop_cancel),
			confirmText = stringResource(R.string.profile_screen_crop_apply),
			style = ProfileImageStyle.DatingPhoto,
			onDismiss = { cropSourceBitmap = null },
			onConfirm = { croppedBitmap ->
				cropSourceBitmap = null
				uploadPreviewBitmap = croppedBitmap
				viewModel.replaceDatingPhoto(
					selectedPhotoIndex = 0,
					imageBytes = croppedBitmap.toPngByteArray(),
					onSaved = { uploadPreviewBitmap = null },
					onUnauthorized = onUnauthorized,
					onNoInternet = onRequestNoInternet
				)
			}
		)
	}
}

@Composable
private fun ProfileCardScene(
	profileCard: ProfileCardUiState,
	previewBitmap: Bitmap?,
	isSubmitting: Boolean,
	error: String?,
	onChangePhotoClick: () -> Unit,
	modifier: Modifier = Modifier
) {
	Column(
		modifier = modifier
			.verticalScroll(rememberScrollState())
			.padding(horizontal = 20.dp, vertical = 6.dp),
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.spacedBy(16.dp)
	) {
		StaticProfileCard(
			profileCard = profileCard,
			previewBitmap = previewBitmap,
			modifier = Modifier
				.fillMaxWidth(0.9f)
				.widthIn(max = 360.dp)
				.aspectRatio(0.74f)
		)

		ProfileAccountInfo(
			profileCard = profileCard,
			modifier = Modifier.fillMaxWidth()
		)

		if (error != null) {
			Surface(
				modifier = Modifier
					.fillMaxWidth()
					.padding(bottom = 12.dp),
				shape = RoundedCornerShape(18.dp),
				color = MaterialTheme.colorScheme.errorContainer,
				contentColor = MaterialTheme.colorScheme.onErrorContainer
			) {
				Text(
					text = error,
					style = MaterialTheme.typography.bodyMedium,
					modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
				)
			}
		}

		AppMediumButton(
			buttonText = stringResource(R.string.profile_screen_change_photo_button),
			isLoading = isSubmitting,
			onClick = onChangePhotoClick
		)

		Spacer(modifier = Modifier.height(12.dp))
	}
}

@Composable
private fun ProfileAccountInfo(
	profileCard: ProfileCardUiState,
	modifier: Modifier = Modifier
) {
	Surface(
		modifier = modifier,
		shape = RoundedCornerShape(22.dp),
		color = MaterialTheme.colorScheme.surface,
		tonalElevation = 2.dp
	) {
		Column(
			modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
			verticalArrangement = Arrangement.spacedBy(12.dp)
		) {
			ProfileInfoRow(
				label = stringResource(R.string.profile_screen_name_label),
				value = profileCard.fullName
			)
			ProfileInfoRow(
				label = stringResource(R.string.profile_screen_age_label),
				value = profileCard.age?.toString()
					?: stringResource(R.string.profile_screen_not_specified)
			)
			ProfileInfoRow(
				label = stringResource(R.string.profile_screen_birth_date_label),
				value = profileCard.birthDateLabel
			)
			ProfileInfoRow(
				label = stringResource(R.string.profile_screen_region_label),
				value = profileCard.region
					?: stringResource(R.string.profile_screen_not_specified)
			)
		}
	}
}

@Composable
private fun ProfileInfoRow(
	label: String,
	value: String,
	modifier: Modifier = Modifier
) {
	Row(
		modifier = modifier.fillMaxWidth(),
		horizontalArrangement = Arrangement.SpaceBetween,
		verticalAlignment = Alignment.CenterVertically
	) {
		Text(
			text = label,
			style = MaterialTheme.typography.bodyMedium,
			color = MaterialTheme.colorScheme.onSurfaceVariant
		)
		Text(
			text = value,
			style = MaterialTheme.typography.titleMedium,
			color = MaterialTheme.colorScheme.onSurface,
			fontWeight = FontWeight.Medium
		)
	}
}

@Composable
private fun StaticProfileCard(
	profileCard: ProfileCardUiState,
	previewBitmap: Bitmap?,
	modifier: Modifier = Modifier
) {
	val dim = LocalDimensions.current
	val shapes = LocalShapes.current
	val extraColors = LocalTmatchExtraColors.current
	val outerShape = RoundedCornerShape(shapes.veryBigRoundedValue)
	val innerShape = RoundedCornerShape(shapes.veryBigRoundedValue - dim.cardOuterPadding)

	CardFrame(
		modifier = modifier,
		outerShape = outerShape,
		innerShape = innerShape
	) {
		DatingPhotoSurface(
			photoUrl = profileCard.datingPhotoUrls.firstOrNull(),
			previewBitmap = previewBitmap,
			title = profileCard.displayTitle(),
			modifier = Modifier.fillMaxSize()
		)
		ScrimInfoBlock {
			Text(
				text = profileCard.displayTitle(),
				style = MaterialTheme.typography.titleLarge,
				color = extraColors.onBaseScrim,
				fontWeight = FontWeight.SemiBold
			)
			profileCard.region?.takeIf { it.isNotBlank() }?.let { region ->
				Surface(
					shape = CircleShape,
					color = extraColors.accentGold
				) {
					Text(
						text = region,
						modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
						style = MaterialTheme.typography.labelLarge,
						color = extraColors.onBaseScrim
					)
				}
			}
		}
	}
}

@Composable
private fun DatingPhotoSurface(
	photoUrl: String?,
	previewBitmap: Bitmap?,
	title: String,
	modifier: Modifier = Modifier
) {
	val remoteBitmap by rememberRemoteBitmap(photoUrl)

	Box(
		modifier = modifier
			.background(MaterialTheme.colorScheme.surfaceContainerHighest),
		contentAlignment = Alignment.Center
	) {
		when {
			previewBitmap != null -> {
				Image(
					bitmap = previewBitmap.asImageBitmap(),
					contentDescription = title,
					contentScale = ContentScale.Crop,
					modifier = Modifier.fillMaxSize()
				)
			}

			remoteBitmap != null -> {
				Image(
					bitmap = remoteBitmap!!.asImageBitmap(),
					contentDescription = title,
					contentScale = ContentScale.Crop,
					modifier = Modifier.fillMaxSize()
				)
			}

			else -> {
				Box(
					modifier = Modifier
						.fillMaxSize()
						.background(
							Color(0xFF10131A)
						),
					contentAlignment = Alignment.Center
				) {
					Text(
						text = title,
						style = MaterialTheme.typography.headlineSmall,
						color = Color.White.copy(alpha = 0.88f),
						modifier = Modifier.padding(24.dp)
					)
				}
			}
		}
	}
}

@Composable
private fun CardFrame(
	modifier: Modifier = Modifier,
	outerShape: RoundedCornerShape,
	innerShape: RoundedCornerShape,
	content: @Composable BoxScope.() -> Unit
) {
	val dim = LocalDimensions.current

	Surface(
		modifier = modifier,
		shape = outerShape,
		color = Color.White
	) {
		Box(
			modifier = Modifier
				.fillMaxSize()
				.padding(dim.cardOuterPadding)
				.clip(innerShape),
			content = content
		)
	}
}

@Composable
private fun BoxScope.ScrimInfoBlock(content: @Composable ColumnScope.() -> Unit) {
	val dim = LocalDimensions.current
	val extraColors = LocalTmatchExtraColors.current

	Column(
		modifier = Modifier
			.align(Alignment.BottomStart)
			.fillMaxWidth()
			.background(extraColors.baseScrim)
			.padding(dim.medium),
		verticalArrangement = Arrangement.spacedBy(dim.small - dim.extraSmall / 2),
		content = content
	)
}

@Composable
private fun ProfileLoadingError(
	message: String,
	onRetry: () -> Unit,
	modifier: Modifier = Modifier
) {
	Column(
		modifier = modifier.fillMaxWidth(),
		verticalArrangement = Arrangement.spacedBy(12.dp),
		horizontalAlignment = Alignment.CenterHorizontally
	) {
		Surface(
			shape = RoundedCornerShape(18.dp),
			color = MaterialTheme.colorScheme.errorContainer,
			contentColor = MaterialTheme.colorScheme.onErrorContainer
		) {
			Text(
				text = message,
				style = MaterialTheme.typography.bodyMedium,
				modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
			)
		}
		AppMediumButton(
			buttonText = stringResource(R.string.profile_screen_retry_button),
			isLoading = false,
			onClick = onRetry
		)
	}
}

@Composable
private fun rememberRemoteBitmap(url: String?): androidx.compose.runtime.State<Bitmap?> {
	return produceState<Bitmap?>(initialValue = null, key1 = url) {
		value = url
			?.takeIf { it.isNotBlank() }
			?.let { loadBitmapFromUrl(it) }
	}
}

private suspend fun loadBitmapFromUrl(url: String): Bitmap? {
	return withContext(Dispatchers.IO) {
		runCatching {
			val connection = URL(url).openConnection().apply {
				connectTimeout = 5_000
				readTimeout = 5_000
			}
			connection.getInputStream().use(BitmapFactory::decodeStream)
		}.getOrNull()
	}
}

private fun ProfileCardUiState.displayTitle(): String {
	return age?.let { "$fullName, $it" } ?: fullName
}
