package com.prod.ui.components.profile

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.image.cropview.CropType
import com.image.cropview.EdgeType
import com.image.cropview.ImageCrop
import com.prod.ui.components.buttons.AppBigButton
import com.prod.ui.components.buttons.AppMediumButton
import java.io.ByteArrayOutputStream

enum class ProfileImageStyle {
	Avatar,
	DatingPhoto
}

@Composable
fun SelectableProfileImage(
	imageBitmap: Bitmap?,
	placeholderText: String,
	contentDescription: String,
	onClick: () -> Unit,
	style: ProfileImageStyle = ProfileImageStyle.Avatar,
	modifier: Modifier = Modifier
) {
	val imageModifier = when (style) {
		ProfileImageStyle.Avatar -> Modifier
			.size(168.dp)
			.clip(CircleShape)

		ProfileImageStyle.DatingPhoto -> Modifier
			.fillMaxWidth(0.62f)
			.height(260.dp)
			.clip(RoundedCornerShape(28.dp))
	}

	Column(
		modifier = modifier.fillMaxWidth(),
		horizontalAlignment = Alignment.CenterHorizontally
	) {
		Box(
			modifier = imageModifier
				.background(MaterialTheme.colorScheme.surfaceContainerHighest)
				.clickable(onClick = onClick),
			contentAlignment = Alignment.Center
		) {
			if (imageBitmap != null) {
				Image(
					bitmap = imageBitmap.asImageBitmap(),
					contentDescription = contentDescription,
					modifier = Modifier.fillMaxSize()
				)
			} else {
				Text(
					text = placeholderText,
					style = MaterialTheme.typography.titleLarge,
					color = MaterialTheme.colorScheme.onSurfaceVariant
				)
			}
		}
	}
}

@Composable
fun ProfileImageCropDialog(
	bitmap: Bitmap,
	title: String,
	cancelText: String,
	confirmText: String,
	style: ProfileImageStyle = ProfileImageStyle.Avatar,
	onDismiss: () -> Unit,
	onConfirm: (Bitmap) -> Unit
) {
	val imageCrop = remember(bitmap) { ImageCrop(bitmap) }
	val cropType = when (style) {
		ProfileImageStyle.Avatar -> CropType.PROFILE_CIRCLE
		ProfileImageStyle.DatingPhoto -> CropType.FREE_STYLE
	}
	val edgeType = when (style) {
		ProfileImageStyle.Avatar -> EdgeType.CIRCULAR
		ProfileImageStyle.DatingPhoto -> EdgeType.SQUARE
	}

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
					text = title,
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
						cropType = cropType,
						edgeType = edgeType,
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
						buttonText = cancelText,
						isLoading = false,
						onClick = onDismiss
					)
					AppBigButton(
						modifier = Modifier.weight(1f),
						buttonText = confirmText,
						isLoading = false,
						onClick = {
							onConfirm(
								when (style) {
									ProfileImageStyle.Avatar -> imageCrop.onCrop()
									ProfileImageStyle.DatingPhoto -> imageCrop.onCrop()
								}
							)
						}
					)
				}
			}
		}
	}
}

fun Context.loadBitmapFromUri(uri: Uri): Bitmap? {
	return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
		contentResolver.openInputStream(uri)?.use(BitmapFactory::decodeStream)
	} else {
		@Suppress("DEPRECATION")
		MediaStore.Images.Media.getBitmap(contentResolver, uri)
	}
}

fun Bitmap.toPngByteArray(): ByteArray {
	return ByteArrayOutputStream().use { outputStream ->
		compress(Bitmap.CompressFormat.PNG, 100, outputStream)
		outputStream.toByteArray()
	}
}