package com.prod.auth.presentation.profile

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.prod.auth.R
import com.prod.auth.presentation.common.AuthFormErrorCard
import com.prod.auth.presentation.common.AuthScreenHeadlineTitle
import com.prod.auth.presentation.common.asString
import com.prod.auth.presentation.common.asStringOrNull
import com.prod.ui.components.AppDatePickerDialog
import com.prod.ui.components.buttons.AppBigButton
import com.prod.ui.components.buttons.AppMediumButton
import com.prod.ui.components.text_fields.AppTextField
import com.image.cropview.CropType
import com.image.cropview.EdgeType
import com.image.cropview.ImageCrop
import org.koin.androidx.compose.koinViewModel

@Composable
fun ProfileSetupScreen(
	onProfileCompleted: () -> Unit,
	onRequestNoInternet: (retryAction: () -> Unit) -> Unit,
	onUnauthorized: () -> Unit,
	modifier: Modifier = Modifier,
	viewModel: ProfileSetupViewModel = koinViewModel()
) {
	val uiState = viewModel.uiState
	val context = LocalContext.current
	var showDatePicker by remember { mutableStateOf(false) }
	var avatarBitmap by remember { mutableStateOf<Bitmap?>(null) }
	var cropSourceBitmap by remember { mutableStateOf<Bitmap?>(null) }

	LaunchedEffect(Unit) {
		viewModel.initialize(
			onProfileReady = onProfileCompleted,
			onNoInternet = onRequestNoInternet,
			onUnauthorized = onUnauthorized
		)
	}

	val pickerLauncher = rememberLauncherForActivityResult(
		contract = ActivityResultContracts.PickVisualMedia()
	) { uri ->
		cropSourceBitmap = uri?.let { context.loadBitmapFromUri(it) }
	}

	Box(
		modifier = modifier
			.fillMaxSize()
			.safeDrawingPadding()
	) {
		if (uiState.isCheckingProfile) {
			CircularProgressIndicator(
				modifier = Modifier.align(Alignment.Center),
				color = MaterialTheme.colorScheme.primary
			)
		} else {
			Column(
				modifier = Modifier
					.fillMaxSize()
					.verticalScroll(rememberScrollState())
					.padding(horizontal = 24.dp, vertical = 16.dp),
				verticalArrangement = Arrangement.spacedBy(16.dp)
			) {
				AuthScreenHeadlineTitle(
					title = stringResource(R.string.profile_setup_title),
					modifier = Modifier.fillMaxWidth()
				)
				ProfileAvatarPicker(
					avatarBitmap = avatarBitmap,
					onPickImage = {
						pickerLauncher.launch(
							PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
						)
					}
				)
				AppTextField(
					value = uiState.firstName,
					onValueChange = viewModel::onFirstNameChange,
					label = stringResource(R.string.profile_setup_first_name),
					error = uiState.firstNameError.asStringOrNull(),
					keyboardOptions = KeyboardOptions(
						keyboardType = KeyboardType.Text,
						imeAction = ImeAction.Next
					)
				)
				AppTextField(
					value = uiState.lastName,
					onValueChange = viewModel::onLastNameChange,
					label = stringResource(R.string.profile_setup_last_name),
					keyboardOptions = KeyboardOptions(
						keyboardType = KeyboardType.Text,
						imeAction = ImeAction.Next
					)
				)
				AppTextField(
					value = uiState.birthDateDisplay,
					onValueChange = {},
					readOnly = true,
					onClick = { showDatePicker = true },
					label = stringResource(R.string.profile_setup_birth_date),
					error = uiState.birthDateError.asStringOrNull(),
					keyboardOptions = KeyboardOptions(
						keyboardType = KeyboardType.Number,
						imeAction = ImeAction.Next
					)
				)
				AppTextField(
					value = uiState.region,
					onValueChange = viewModel::onRegionChange,
					label = stringResource(R.string.profile_setup_region),
					keyboardOptions = KeyboardOptions(
						keyboardType = KeyboardType.Text,
						imeAction = ImeAction.Done
					)
				)
				uiState.formError?.let { message ->
					AuthFormErrorCard(
						message = message.asString(),
						modifier = Modifier.fillMaxWidth()
					)
				}
				AppBigButton(
					buttonText = stringResource(R.string.profile_setup_submit),
					isLoading = uiState.isSubmitting,
					onClick = {
						viewModel.submit(
							avatarBitmap = avatarBitmap,
							onSuccess = onProfileCompleted,
							onNoInternet = onRequestNoInternet,
							onUnauthorized = onUnauthorized
						)
					}
				)
			}
		}
	}

	if (showDatePicker) {
		AppDatePickerDialog(
			onDismissRequest = { showDatePicker = false },
			onDateSelected = viewModel::onBirthDateSelected
		)
	}

	cropSourceBitmap?.let { sourceBitmap ->
		ProfileImageCropDialog(
			bitmap = sourceBitmap,
			onDismiss = { cropSourceBitmap = null },
			onConfirm = { croppedBitmap ->
				avatarBitmap = croppedBitmap
				cropSourceBitmap = null
			}
		)
	}
}

@Composable
private fun ProfileAvatarPicker(
	avatarBitmap: Bitmap?,
	onPickImage: () -> Unit
) {
	Column(
		modifier = Modifier.fillMaxWidth(),
		horizontalAlignment = Alignment.CenterHorizontally
	) {
		Box(
			modifier = Modifier
				.size(144.dp)
				.clip(CircleShape)
				.background(MaterialTheme.colorScheme.surfaceContainerHighest)
				.clickable(onClick = onPickImage),
			contentAlignment = Alignment.Center
		) {
			if (avatarBitmap != null) {
				Image(
					bitmap = avatarBitmap.asImageBitmap(),
					contentDescription = stringResource(R.string.profile_setup_avatar_content_description),
					modifier = Modifier.fillMaxSize()
				)
			} else {
				Text(
					text = stringResource(R.string.profile_setup_avatar_placeholder),
					style = MaterialTheme.typography.titleLarge,
					color = MaterialTheme.colorScheme.onSurfaceVariant
				)
			}
		}
	}
}

@Composable
private fun ProfileImageCropDialog(
	bitmap: Bitmap,
	onDismiss: () -> Unit,
	onConfirm: (Bitmap) -> Unit
) {
	val imageCrop = remember(bitmap) { ImageCrop(bitmap) }

	Dialog(
		onDismissRequest = onDismiss,
		properties = DialogProperties(usePlatformDefaultWidth = false)
	) {
		Surface(
			modifier = Modifier.fillMaxSize(),
			color = MaterialTheme.colorScheme.surface
		) {
			Column(
				modifier = Modifier
					.fillMaxSize()
					.padding(24.dp),
				verticalArrangement = Arrangement.spacedBy(16.dp)
			) {
				Text(
					text = stringResource(R.string.profile_setup_crop_title),
					style = MaterialTheme.typography.headlineSmall,
					color = MaterialTheme.colorScheme.onSurface
				)
				Box(
					modifier = Modifier
						.weight(1f)
						.fillMaxWidth()
				) {
					imageCrop.ImageCropView(
						modifier = Modifier.fillMaxSize(),
						cropType = CropType.PROFILE_CIRCLE,
						edgeType = EdgeType.CIRCULAR,
						showGuideLines = true,
						guideLineColor = Color.White.copy(alpha = 0.5f)
					)
				}
				Row(
					modifier = Modifier.fillMaxWidth(),
					horizontalArrangement = Arrangement.spacedBy(12.dp)
				) {
					AppMediumButton(
						modifier = Modifier.weight(1f),
						buttonText = stringResource(R.string.profile_setup_cancel_crop),
						isLoading = false,
						onClick = onDismiss
					)
					AppBigButton(
						modifier = Modifier.weight(1f),
						buttonText = stringResource(R.string.profile_setup_apply_crop),
						isLoading = false,
						onClick = {
							onConfirm(imageCrop.onCrop())
						}
					)
				}
			}
		}
	}
}

private fun Context.loadBitmapFromUri(uri: Uri): Bitmap? {
	return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
		contentResolver.openInputStream(uri)?.use(BitmapFactory::decodeStream)
	} else {
		@Suppress("DEPRECATION")
		MediaStore.Images.Media.getBitmap(contentResolver, uri)
	}
}
