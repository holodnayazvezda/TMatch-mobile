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
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.prod.core.ui.theme.Dimensions
import com.prod.core.ui.theme.Elevations
import com.prod.core.ui.theme.LocalDimensions
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
	val view = LocalView.current

	if (!view.isInEditMode) {
		val activity = view.context.findActivity()
		if (activity != null) {
			@Suppress("DEPRECATION")
			SideEffect {
				val window = activity.window
				window.statusBarColor = colorScheme.primaryContainer.toArgb()
				window.navigationBarColor = colorScheme.surfaceContainerHighest.toArgb()

				WindowCompat.getInsetsController(window, view).apply {
					isAppearanceLightStatusBars = !darkTheme
					isAppearanceLightNavigationBars = !darkTheme
				}
			}
		}
	}
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
