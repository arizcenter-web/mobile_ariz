package com.tecsup.app_ariz.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

/** Caja de error del web (`AuthAlert`): fondo `error` al 10 %, texto `error`. */
@Composable
fun ArizAlert(message: String, modifier: Modifier = Modifier) {
    Text(
        text = message,
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.error.copy(alpha = 0.1f))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.error,
    )
}
