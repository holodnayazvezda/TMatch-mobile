package com.prod.ui.components.buttons

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.prod.core.ui.theme.LocalShapes
import com.prod.ui.theme.LocalDimensions

@Composable
fun AppBigButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    buttonText: String,
    isLoading: Boolean
) {
    val colorScheme = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val dim = LocalDimensions.current
    val shapes = LocalShapes.current
    Button(
        modifier = modifier
            .fillMaxWidth()
            .height(dim.actionButtonHeight),
        shape = shapes.buttonRounded,
        onClick = onClick,
        enabled = !isLoading,
        colors = ButtonDefaults.buttonColors(
            containerColor = colorScheme.primary,
            contentColor = colorScheme.onPrimary,
            disabledContainerColor = colorScheme.surfaceContainerHighest,
            disabledContentColor = colorScheme.onSurface.copy(alpha = 0.38f)
        )
    ){
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(dim.iconSmall),
                color = colorScheme.onPrimary,
                strokeWidth = dim.extraSmall / 2 + dim.extraSmall / 8
            )
        } else {
            Text(
                text = buttonText,
                style = typography.titleMedium,
            )
        }
    }
}
