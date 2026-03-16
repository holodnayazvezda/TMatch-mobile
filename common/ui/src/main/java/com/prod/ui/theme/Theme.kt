package com.prod.ui.theme

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.prod.core.ui.theme.Elevations
import com.prod.core.ui.theme.LocalElevation
import com.prod.core.ui.theme.LocalShapes
import com.prod.core.ui.theme.Shapes

private val LightColorScheme = lightColorScheme(
	primary = LightPrimary,
	onPrimary = LightOnPrimary,
	primaryContainer = LightPrimaryContainer,
	onPrimaryContainer = LightOnPrimaryContainer,
	inversePrimary = LightInversePrimary,
	secondary = LightSecondary,
	onSecondary = LightOnSecondary,
	secondaryContainer = LightSecondaryContainer,
	onSecondaryContainer = LightOnSecondaryContainer,
	tertiary = LightTertiary,
	onTertiary = LightOnTertiary,
	tertiaryContainer = LightTertiaryContainer,
	onTertiaryContainer = LightOnTertiaryContainer,
	error = LightError,
	onError = LightOnError,
	errorContainer = LightErrorContainer,
	onErrorContainer = LightOnErrorContainer,
	background = LightBackground,
	onBackground = LightOnBackground,
	surface = LightSurface,
	onSurface = LightOnSurface,
	surfaceVariant = LightSurfaceVariant,
	onSurfaceVariant = LightOnSurfaceVariant,
	inverseSurface = LightInverseSurface,
	inverseOnSurface = LightInverseOnSurface,
	outline = LightOutline,
	outlineVariant = LightOutlineVariant,
	scrim = LightScrim,
	surfaceTint = LightSurfaceTint,
	surfaceDim = LightSurfaceDim,
	surfaceBright = LightSurfaceBright,
	surfaceContainerLowest = LightSurfaceContainerLowest,
	surfaceContainerLow = LightSurfaceContainerLow,
	surfaceContainer = LightSurfaceContainer,
	surfaceContainerHigh = LightSurfaceContainerHigh,
	surfaceContainerHighest = LightSurfaceContainerHighest
)

private val DarkColorScheme = darkColorScheme(
	primary = DarkPrimary,
	onPrimary = DarkOnPrimary,
	primaryContainer = DarkPrimaryContainer,
	onPrimaryContainer = DarkOnPrimaryContainer,
	inversePrimary = DarkInversePrimary,
	secondary = DarkSecondary,
	onSecondary = DarkOnSecondary,
	secondaryContainer = DarkSecondaryContainer,
	onSecondaryContainer = DarkOnSecondaryContainer,
	tertiary = DarkTertiary,
	onTertiary = DarkOnTertiary,
	tertiaryContainer = DarkTertiaryContainer,
	onTertiaryContainer = DarkOnTertiaryContainer,
	error = DarkError,
	onError = DarkOnError,
	errorContainer = DarkErrorContainer,
	onErrorContainer = DarkOnErrorContainer,
	background = DarkBackground,
	onBackground = DarkOnBackground,
	surface = DarkSurface,
	onSurface = DarkOnSurface,
	surfaceVariant = DarkSurfaceVariant,
	onSurfaceVariant = DarkOnSurfaceVariant,
	inverseSurface = DarkInverseSurface,
	inverseOnSurface = DarkInverseOnSurface,
	outline = DarkOutline,
	outlineVariant = DarkOutlineVariant,
	scrim = DarkScrim,
	surfaceTint = DarkSurfaceTint,
	surfaceDim = DarkSurfaceDim,
	surfaceBright = DarkSurfaceBright,
	surfaceContainerLowest = DarkSurfaceContainerLowest,
	surfaceContainerLow = DarkSurfaceContainerLow,
	surfaceContainer = DarkSurfaceContainer,
	surfaceContainerHigh = DarkSurfaceContainerHigh,
	surfaceContainerHighest = DarkSurfaceContainerHighest
)

@Composable
fun TmatchTheme(
	darkTheme: Boolean = isSystemInDarkTheme(),
	content: @Composable () -> Unit
) {
	val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
	val extraColors = if (darkTheme) DarkExtraColors else LightExtraColors

	CompositionLocalProvider(
		LocalDimensions provides Dimensions(),
		LocalElevation provides Elevations(),
		LocalShapes provides Shapes(),
		LocalTmatchExtraColors provides extraColors
	) {
		MaterialTheme(
			colorScheme = colorScheme,
			typography = Typography,
			content = content
		)
	}
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
	is Activity -> this
	is ContextWrapper -> baseContext.findActivity()
	else -> null
}
