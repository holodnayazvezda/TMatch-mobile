package com.prod.ui.components.buttons

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


@Composable
fun AppMediumButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    buttonText: String,
    isLoading: Boolean
) {
    val colorScheme = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    Button(
        modifier = modifier
            .fillMaxWidth()
            .height(55.dp),
        shape = RoundedCornerShape(10.dp),
        onClick = onClick,
        enabled = !isLoading,
        colors = ButtonDefaults.buttonColors(
            containerColor = colorScheme.secondaryContainer,
            contentColor = colorScheme.onSecondaryContainer,
            disabledContainerColor = colorScheme.surfaceContainerHighest,
            disabledContentColor = colorScheme.onSurface.copy(alpha = 0.38f)
        )
    ){
        if (isLoading) {
            CircularProgressIndicator(color = colorScheme.onSecondaryContainer)
        } else {
            Text(
                text = buttonText,
                style = typography.titleMedium,
            )
        }
    }
}
