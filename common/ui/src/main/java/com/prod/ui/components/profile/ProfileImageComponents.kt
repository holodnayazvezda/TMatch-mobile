package com.prod.ui.components.profile

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
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
import androidx.compose.ui.layout.ContentScale
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
import com.tanishranjan.cropkit.CropDefaults
import com.tanishranjan.cropkit.CropRatio
import com.tanishranjan.cropkit.CropShape
import com.tanishranjan.cropkit.GridLinesType
import com.tanishranjan.cropkit.GridLinesVisibility
import com.tanishranjan.cropkit.ImageCropper
import com.tanishranjan.cropkit.rememberCropController
import com.prod.ui.components.buttons.AppBigButton
import com.prod.ui.components.buttons.AppMediumButton
import java.io.ByteArrayOutputStream
import kotlin.math.max
import kotlin.math.roundToInt

enum class ProfileImageStyle {
	Avatar,
	DatingPhoto
}

private const val CropInputMaxDimension = 2048

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
	val colorScheme = MaterialTheme.colorScheme
	val cropOptions = remember(style) {
		when (style) {
			ProfileImageStyle.Avatar -> CropDefaults.cropOptions(
				cropShape = CropShape.AspectRatio(CropRatio.SQUARE),
				contentScale = ContentScale.Fit,
				gridLinesVisibility = GridLinesVisibility.ALWAYS,
				gridLinesType = GridLinesType.CIRCLE
			)

			ProfileImageStyle.DatingPhoto -> CropDefaults.cropOptions(
				cropShape = CropShape.AspectRatio(CropRatio.PORTRAIT_3_4),
				contentScale = ContentScale.Fit,
				gridLinesVisibility = GridLinesVisibility.ON_TOUCH,
				gridLinesType = GridLinesType.GRID
			)
		}
	}
	val cropColors = remember(
		colorScheme.scrim,
		colorScheme.primary,
		colorScheme.onSurface
	) {
		CropDefaults.cropColors(
			overlay = colorScheme.scrim.copy(alpha = 0.52f),
			overlayActive = colorScheme.scrim.copy(alpha = 0.28f),
			gridlines = Color.White.copy(alpha = 0.55f),
			cropRectangle = Color.White.copy(alpha = 0.92f),
			handle = colorScheme.primary
		)
	}
	val cropController = rememberCropController(
		bitmap = bitmap,
		cropOptions = cropOptions,
		cropColors = cropColors
	)

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
					ImageCropper(
						modifier = Modifier.fillMaxSize(),
						cropController = cropController
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
							onConfirm(cropController.crop())
						}
					)
				}
			}
		}
	}
}

fun Context.loadBitmapForCroppingFromUri(uri: Uri): Bitmap? {
	return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
		loadBitmapFromUriModern(
			uri = uri,
			maxDimension = CropInputMaxDimension
		) ?: loadBitmapFromUriSampled(
			uri = uri,
			maxDimension = CropInputMaxDimension
		)
	} else {
		loadBitmapFromUriSampled(
			uri = uri,
			maxDimension = CropInputMaxDimension
		) ?: loadBitmapFromUriLegacy(
			uri = uri,
			maxDimension = CropInputMaxDimension
		)
	}
}

private fun Context.loadBitmapFromUriModern(
	uri: Uri,
	maxDimension: Int
): Bitmap? {
	return runCatching {
		val source = ImageDecoder.createSource(contentResolver, uri)
		ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
			decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
			decoder.isMutableRequired = true
			decoder.memorySizePolicy = ImageDecoder.MEMORY_POLICY_LOW_RAM

			val width = info.size.width
			val height = info.size.height
			val longestSide = max(width, height)
			if (longestSide > maxDimension) {
				val scale = maxDimension.toFloat() / longestSide.toFloat()
				decoder.setTargetSize(
					(width * scale).roundToInt().coerceAtLeast(1),
					(height * scale).roundToInt().coerceAtLeast(1)
				)
			}
		}
	}.getOrNull()
}

private fun Context.loadBitmapFromUriSampled(
	uri: Uri,
	maxDimension: Int
): Bitmap? {
	val boundsOptions = BitmapFactory.Options().apply {
		inJustDecodeBounds = true
	}
	contentResolver.openInputStream(uri)?.use { inputStream ->
		BitmapFactory.decodeStream(inputStream, null, boundsOptions)
	} ?: return null

	if (boundsOptions.outWidth <= 0 || boundsOptions.outHeight <= 0) {
		return null
	}

	val decodeOptions = BitmapFactory.Options().apply {
		inSampleSize = calculateInSampleSize(
			width = boundsOptions.outWidth,
			height = boundsOptions.outHeight,
			maxDimension = maxDimension
		)
		inPreferredConfig = Bitmap.Config.RGB_565
	}

	val decodedBitmap = contentResolver.openInputStream(uri)?.use { inputStream ->
		BitmapFactory.decodeStream(inputStream, null, decodeOptions)
	} ?: return null

	return decodedBitmap.scaleDownIfNeeded(maxDimension = maxDimension)
}

private fun Context.loadBitmapFromUriLegacy(
	uri: Uri,
	maxDimension: Int
): Bitmap? {
	return runCatching {
		@Suppress("DEPRECATION")
		MediaStore.Images.Media.getBitmap(contentResolver, uri)
	}.getOrNull()?.scaleDownIfNeeded(maxDimension = maxDimension)
}

fun Bitmap.toPngByteArray(): ByteArray {
	return ByteArrayOutputStream().use { outputStream ->
		compress(Bitmap.CompressFormat.PNG, 100, outputStream)
		outputStream.toByteArray()
	}
}

private fun calculateInSampleSize(
	width: Int,
	height: Int,
	maxDimension: Int
): Int {
	var sampleSize = 1

	while (
		width / (sampleSize * 2) >= maxDimension ||
		height / (sampleSize * 2) >= maxDimension
	) {
		sampleSize *= 2
	}

	return sampleSize
}

private fun Bitmap.scaleDownIfNeeded(maxDimension: Int): Bitmap {
	val longestSide = max(width, height)
	if (longestSide <= maxDimension) {
		return this
	}

	val scale = maxDimension.toFloat() / longestSide.toFloat()
	val scaledBitmap = Bitmap.createScaledBitmap(
		this,
		(width * scale).roundToInt().coerceAtLeast(1),
		(height * scale).roundToInt().coerceAtLeast(1),
		true
	)

	if (scaledBitmap !== this) {
		recycle()
	}

	return scaledBitmap
}
