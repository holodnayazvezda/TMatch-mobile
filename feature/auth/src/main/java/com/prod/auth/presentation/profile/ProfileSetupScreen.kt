package com.prod.auth.presentation.profile

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.prod.auth.R
import com.prod.auth.presentation.common.AuthFormErrorCard
import com.prod.auth.presentation.common.AuthScreenHeadlineTitle
import com.prod.auth.presentation.common.asString
import com.prod.auth.presentation.common.asStringOrNull
import com.prod.ui.components.AppDatePickerDialog
import com.prod.ui.components.buttons.AppBigButton
import com.prod.ui.components.profile.ProfileImageCropDialog
import com.prod.ui.components.profile.ProfileImageStyle
import com.prod.ui.components.profile.SelectableProfileImage
import com.prod.ui.components.profile.loadBitmapFromUri
import com.prod.ui.components.text_fields.AppTextField
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
				SelectableProfileImage(
					imageBitmap = avatarBitmap,
					placeholderText = stringResource(R.string.profile_setup_avatar_placeholder),
					contentDescription = stringResource(R.string.profile_setup_avatar_content_description),
					style = ProfileImageStyle.Avatar,
					onClick = {
						pickerLauncher.launch(
							PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
						)
					},
					modifier = Modifier.fillMaxWidth()
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
			title = stringResource(R.string.profile_setup_crop_title),
			cancelText = stringResource(R.string.profile_setup_cancel_crop),
			confirmText = stringResource(R.string.profile_setup_apply_crop),
			style = ProfileImageStyle.Avatar,
			onDismiss = { cropSourceBitmap = null },
			onConfirm = { croppedBitmap ->
				avatarBitmap = croppedBitmap
				cropSourceBitmap = null
			}
		)
	}
}
