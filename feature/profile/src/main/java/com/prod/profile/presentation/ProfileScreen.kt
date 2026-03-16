package com.prod.profile.presentation

import android.graphics.Bitmap
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.SingletonImageLoader
import coil3.compose.SubcomposeAsyncImage
import com.prod.core.ui.theme.LocalDimensions
import com.prod.profile.R
import com.prod.ui.components.AppBottomScrimInfo
import com.prod.ui.components.AppHeroCardFrame
import com.prod.ui.components.AppHeroPagerIndicator
import com.prod.ui.components.AppInfoChip
import com.prod.ui.components.AppScreenTopBar
import com.prod.ui.components.AppFactCard
import com.prod.ui.components.AppSectionCard
import com.prod.ui.components.AppSectionTitle
import com.prod.ui.components.buttons.AppBigButton
import com.prod.ui.components.buttons.AppMediumButton
import com.prod.ui.components.profile.ProfileImageCropDialog
import com.prod.ui.components.profile.ProfileImageStyle
import com.prod.ui.components.profile.loadBitmapForCroppingFromUri
import com.prod.ui.components.profile.toPngByteArray
import com.prod.ui.theme.LocalTmatchExtraColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.compose.koinViewModel
import java.io.File

@Composable
fun ProfileScreen(
	onUnauthorized: () -> Unit,
	onProfileMissing: () -> Unit,
	onLogout: () -> Unit,
	onRequestNoInternet: (retryAction: () -> Unit) -> Unit,
	modifier: Modifier = Modifier,
	viewModel: ProfileViewModel = koinViewModel()
) {
	val uiState = viewModel.uiState
	val context = LocalContext.current
	val scope = rememberCoroutineScope()
	var cropSourceBitmap by remember { mutableStateOf<Bitmap?>(null) }
	var showLogoutDialog by remember { mutableStateOf(false) }
	var isLoggingOut by remember { mutableStateOf(false) }

	val pickerLauncher = rememberLauncherForActivityResult(
		contract = ActivityResultContracts.PickVisualMedia()
	) { uri ->
		cropSourceBitmap = uri?.let { context.loadBitmapForCroppingFromUri(it) }
	}

	LaunchedEffect(Unit) {
		viewModel.initialize(
			onUnauthorized = onUnauthorized,
			onProfileMissing = onProfileMissing,
			onNoInternet = onRequestNoInternet
		)
	}

	Box(
		modifier = modifier.fillMaxSize()
	) {
		when {
			uiState.profileCard != null -> {
				ProfileCardScene(
					profileCard = uiState.profileCard,
					isSubmitting = uiState.isSubmitting,
						isLoggingOut = isLoggingOut,
						error = uiState.error,
						onChangePhotoClick = {
							pickerLauncher.launch(
							PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
						)
						},
						onLogoutClick = { showLogoutDialog = true },
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
							onProfileMissing = onProfileMissing,
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

	if (showLogoutDialog) {
		AlertDialog(
			onDismissRequest = {
				if (!isLoggingOut) {
					showLogoutDialog = false
				}
			},
			title = {
				Text(text = stringResource(R.string.profile_screen_logout_dialog_title))
			},
			confirmButton = {
				TextButton(
					enabled = !isLoggingOut,
					onClick = {
						isLoggingOut = true
						scope.launch {
							clearProfileScreenCache(context)
							showLogoutDialog = false
							isLoggingOut = false
							onLogout()
						}
					}
				) {
					Text(text = stringResource(R.string.profile_screen_logout_confirm))
				}
			},
			dismissButton = {
				TextButton(
					enabled = !isLoggingOut,
					onClick = { showLogoutDialog = false }
				) {
					Text(text = stringResource(R.string.profile_screen_logout_cancel))
				}
			}
		)
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
				viewModel.replaceDatingPhoto(
					selectedPhotoIndex = 0,
					imageBytes = croppedBitmap.toPngByteArray(),
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
	isSubmitting: Boolean,
	isLoggingOut: Boolean,
	error: String?,
	onChangePhotoClick: () -> Unit,
	onLogoutClick: () -> Unit,
	modifier: Modifier = Modifier
) {
	val dim = LocalDimensions.current
	Column(
		modifier = modifier
	) {
		ProfileHeader(
			onLogoutClick = onLogoutClick,
			isLogoutEnabled = !isLoggingOut
		)

		Column(
			modifier = Modifier
				.weight(1f)
				.verticalScroll(rememberScrollState())
				.padding(horizontal = dim.medium, vertical = dim.cardOuterPadding),
			horizontalAlignment = Alignment.CenterHorizontally,
			verticalArrangement = Arrangement.Top
		) {
			Column(
				modifier = Modifier
					.fillMaxWidth()
					.widthIn(max = 360.dp),
				verticalArrangement = Arrangement.spacedBy(16.dp),
				horizontalAlignment = Alignment.CenterHorizontally
			) {
				val contentCardModifier = Modifier.fillMaxWidth()

				StaticProfileCard(
					profileCard = profileCard,
					isSubmitting = isSubmitting,
					onClick = onChangePhotoClick,
					modifier = contentCardModifier
						.aspectRatio(0.74f)
				)

				ProfileAboutSection(
					profileCard = profileCard,
					modifier = contentCardModifier
				)

				ProfilePhotoActionsSection(
					isSubmitting = isSubmitting,
					onChangePhotoClick = onChangePhotoClick,
					modifier = contentCardModifier
				)

				if (error != null) {
					AppSectionCard(
						modifier = contentCardModifier,
						containerColor = MaterialTheme.colorScheme.errorContainer,
						contentColor = MaterialTheme.colorScheme.onErrorContainer,
						shape = RoundedCornerShape(18.dp),
						tonalElevation = 0.dp
					) {
						Text(
							text = error,
							style = MaterialTheme.typography.bodyMedium
						)
					}
				}

				Spacer(modifier = Modifier.height(12.dp))
			}
		}
	}
}

@Composable
private fun ProfileHeader(
	onLogoutClick: () -> Unit,
	isLogoutEnabled: Boolean,
	modifier: Modifier = Modifier
) {
	val dim = LocalDimensions.current
	AppScreenTopBar(
		title = stringResource(R.string.profile_screen_title),
		modifier = modifier
			.fillMaxWidth()
			.padding(
				start = dim.verySmall,
				top = dim.verySmall,
				end = dim.small,
				bottom = dim.extraSmall
			),
		actions = {
			IconButton(
				enabled = isLogoutEnabled,
				onClick = onLogoutClick
			) {
				Icon(
					painter = painterResource(R.drawable.logout),
					contentDescription = stringResource(R.string.profile_screen_logout_button),
					tint = MaterialTheme.colorScheme.onBackground,
					modifier = Modifier.size(22.dp)
				)
			}
		}
	)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ProfileAboutSection(
	profileCard: ProfileCardUiState,
	modifier: Modifier = Modifier
) {
	val dim = LocalDimensions.current
	AppSectionCard(
		modifier = modifier,
		containerColor = MaterialTheme.colorScheme.surface
	) {
		Column(verticalArrangement = Arrangement.spacedBy(dim.small + dim.extraSmall / 2)) {
			Row(
				modifier = Modifier.fillMaxWidth(),
				horizontalArrangement = Arrangement.SpaceBetween,
				verticalAlignment = Alignment.Top
			) {
				Column(
					modifier = Modifier.weight(1f),
					verticalArrangement = Arrangement.spacedBy(4.dp)
				) {
					AppSectionTitle(title = stringResource(R.string.profile_screen_details_title))
				}
				AppInfoChip(
					text = stringResource(
						R.string.profile_screen_photo_count,
						profileCard.datingPhotoUrls.size.coerceAtLeast(1)
					),
					containerColor = MaterialTheme.colorScheme.tertiaryContainer,
					contentColor = MaterialTheme.colorScheme.onTertiaryContainer
				)
			}

			FlowRow(
				horizontalArrangement = Arrangement.spacedBy(dim.small - dim.extraSmall / 2),
				verticalArrangement = Arrangement.spacedBy(dim.small - dim.extraSmall / 2)
			) {
				AppFactCard(
					title = stringResource(R.string.profile_screen_age_label),
					value = profileCard.age?.toString()
						?: stringResource(R.string.profile_screen_not_specified),
					modifier = Modifier.fillMaxWidth(0.48f)
				)
				AppFactCard(
					title = stringResource(R.string.profile_screen_birth_date_label),
					value = profileCard.birthDateLabel,
					modifier = Modifier.fillMaxWidth(0.48f)
				)
				AppFactCard(
					title = stringResource(R.string.profile_screen_name_label),
					value = profileCard.fullName,
					modifier = Modifier.fillMaxWidth()
				)
				AppFactCard(
					title = stringResource(R.string.profile_screen_region_label),
					value = profileCard.region
						?: stringResource(R.string.profile_screen_not_specified),
					modifier = Modifier.fillMaxWidth()
				)
			}
		}
	}
}

@Composable
private fun ProfilePhotoActionsSection(
	isSubmitting: Boolean,
	onChangePhotoClick: () -> Unit,
	modifier: Modifier = Modifier
) {
	val dim = LocalDimensions.current
	AppSectionCard(
		modifier = modifier,
		containerColor = MaterialTheme.colorScheme.surface
	) {
		Column(verticalArrangement = Arrangement.spacedBy(dim.small)) {
			AppSectionTitle(title = stringResource(R.string.profile_screen_actions_title))
			AppBigButton(
				buttonText = stringResource(R.string.profile_screen_change_photo_button),
				isLoading = isSubmitting,
				onClick = onChangePhotoClick
			)
			AppMediumButton(
				buttonText = stringResource(R.string.profile_screen_change_photo_hint),
				isLoading = false,
				onClick = onChangePhotoClick
			)
		}
	}
}

@Composable
private fun StaticProfileCard(
	profileCard: ProfileCardUiState,
	isSubmitting: Boolean,
	onClick: () -> Unit,
	modifier: Modifier = Modifier
) {
	val heroImages = profileCard.datingPhotoUrls.ifEmpty { listOf<String?>(null) }
	val displayTitle = profileCard.age?.let {
		stringResource(R.string.profile_display_title_with_age, profileCard.fullName, it)
	} ?: profileCard.fullName
	val extraColors = LocalTmatchExtraColors.current
	val pagerState = rememberPagerState(
		initialPage = 0,
		pageCount = { heroImages.size }
	)

	AppHeroCardFrame(
		modifier = modifier.clickable(
			enabled = !isSubmitting,
			onClick = onClick
		)
	) {
		HorizontalPager(
			state = pagerState,
			modifier = Modifier.fillMaxSize(),
			pageSpacing = 0.dp
		) { index ->
			DatingPhotoSurface(
				photoUrl = heroImages[index],
				isUploading = isSubmitting,
				title = displayTitle,
				modifier = Modifier.fillMaxSize()
			)
		}
		AppHeroPagerIndicator(
			total = heroImages.size,
			current = pagerState.currentPage
		)
		AppBottomScrimInfo {
			Text(
				text = displayTitle,
				style = MaterialTheme.typography.titleLarge,
				color = extraColors.onBaseScrim,
				fontWeight = FontWeight.SemiBold
			)
			AppInfoChip(
				text = stringResource(
					R.string.profile_screen_photo_count,
					profileCard.datingPhotoUrls.size.coerceAtLeast(1)
				),
				containerColor = extraColors.accentGold,
				contentColor = extraColors.onBaseScrim
			)
		}
	}
}

@Composable
private fun DatingPhotoSurface(
	photoUrl: String?,
	isUploading: Boolean,
	title: String,
	modifier: Modifier = Modifier
) {
	val hasPhoto = !photoUrl.isNullOrBlank()

	Box(
		modifier = modifier
			.background(MaterialTheme.colorScheme.surfaceContainerHighest),
		contentAlignment = Alignment.Center
	) {
		when {
			isUploading -> UploadingDatingPhotoState()

			hasPhoto -> {
				SubcomposeAsyncImage(
					model = photoUrl,
					contentDescription = title,
					contentScale = ContentScale.Crop,
					modifier = Modifier.fillMaxSize(),
					loading = {
						UploadingDatingPhotoState()
					},
					error = {
						EmptyDatingPhotoSurface()
					}
				)
			}

			else -> {
				EmptyDatingPhotoSurface(
					prompt = stringResource(R.string.profile_screen_empty_photo_prompt)
				)
			}
		}
	}
}

@Composable
private fun UploadingDatingPhotoState(
	modifier: Modifier = Modifier
) {
	Column(
		modifier = modifier
			.fillMaxSize()
			.background(MaterialTheme.colorScheme.inverseSurface)
			.padding(24.dp),
		verticalArrangement = Arrangement.Center,
		horizontalAlignment = Alignment.CenterHorizontally
	) {
		CircularProgressIndicator(
			color = MaterialTheme.colorScheme.primary
		)
	}
}

@Composable
private fun EmptyDatingPhotoSurface(
	prompt: String? = null,
	modifier: Modifier = Modifier
) {
	Box(
		modifier = modifier
			.fillMaxSize()
			.background(MaterialTheme.colorScheme.inverseSurface),
		contentAlignment = Alignment.Center
	) {
		prompt?.let {
			Text(
				text = it,
				style = MaterialTheme.typography.titleMedium,
				color = MaterialTheme.colorScheme.inverseOnSurface,
				modifier = Modifier.padding(horizontal = 24.dp)
			)
		}
	}
}

@Composable
internal fun ProfileLoadingError(
	message: String,
	onRetry: () -> Unit,
	modifier: Modifier = Modifier
) {
	Column(
		modifier = modifier.fillMaxWidth(),
		verticalArrangement = Arrangement.spacedBy(12.dp),
		horizontalAlignment = Alignment.CenterHorizontally
	) {
		AppSectionCard(
			containerColor = MaterialTheme.colorScheme.errorContainer,
			contentColor = MaterialTheme.colorScheme.onErrorContainer,
			shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp),
			tonalElevation = 0.dp
		) {
			Text(
				text = message,
				style = MaterialTheme.typography.bodyMedium
			)
		}
		AppMediumButton(
			buttonText = stringResource(R.string.profile_screen_retry_button),
			isLoading = false,
			onClick = onRetry
		)
	}
}

private suspend fun clearProfileScreenCache(context: Context) {
	withContext(Dispatchers.IO) {
		val imageLoader = SingletonImageLoader.get(context)
		runCatching {
			imageLoader.memoryCache?.clear()
		}
		runCatching {
			imageLoader.diskCache?.clear()
		}
		context.cacheDir.clearContents()
		context.externalCacheDir?.clearContents()
	}
}

private fun File.clearContents() {
	listFiles()?.forEach { child ->
		runCatching { child.deleteRecursively() }
	}
}
