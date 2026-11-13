package com.tecsup.app_ariz.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.tecsup.app_ariz.core.designsystem.theme.ArizColors

private val FavoriteRed = Color(0xFFEF4444)

/**
 * `FavoriteButton` del web: círculo blanco con un corazón, relleno y rojo cuando la propiedad está
 * guardada. Quien lo usa decide qué pasa al tocarlo (guardar, quitar o pedir iniciar sesión).
 */
@Composable
fun FavoriteHeart(
    isFavorite: Boolean,
    onClick: () -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(36.dp)
            .shadow(3.dp, CircleShape)
            .background(Color.White, CircleShape)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
            contentDescription = contentDescription,
            modifier = Modifier.size(18.dp),
            tint = if (isFavorite) FavoriteRed else ArizColors.Brand,
        )
    }
}
