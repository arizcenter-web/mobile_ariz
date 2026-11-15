package com.tecsup.app_ariz.feature.favorites.ui

import com.tecsup.app_ariz.core.ui.component.ConfirmTone
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import com.tecsup.app_ariz.core.ui.component.ConfirmDialog
import com.tecsup.app_ariz.core.ui.statusBarScrim
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tecsup.app_ariz.core.designsystem.component.ArizPrimaryButton
import com.tecsup.app_ariz.core.designsystem.theme.ArizColors
import com.tecsup.app_ariz.core.designsystem.theme.ArizTheme
import com.tecsup.app_ariz.core.favorites.domain.Favorite
import com.tecsup.app_ariz.core.session.SessionState
import com.tecsup.app_ariz.core.ui.LightStatusBarIcons
import com.tecsup.app_ariz.core.ui.component.FavoriteHeart
import com.tecsup.app_ariz.core.ui.component.TabHeader
import com.tecsup.app_ariz.feature.favorites.R
import java.text.SimpleDateFormat
import java.util.Locale

private val FavoriteSoft = Color(0xFFFF8A8A)

@Composable
fun FavoritesScreen(
    onPropertyClick: (slug: String) -> Unit,
    onLoginClick: () -> Unit,
    onExploreClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FavoritesListViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    FavoritesContent(
        state = state,
        onAction = viewModel::onAction,
        onPropertyClick = onPropertyClick,
        onLoginClick = onLoginClick,
        onExploreClick = onExploreClick,
        modifier = modifier,
    )
}

/** Contenido sin estado: se previsualiza y se prueba pasándole un [FavoritesUiState]. */
@Composable
fun FavoritesContent(
    state: FavoritesUiState,
    onAction: (FavoritesAction) -> Unit,
    onPropertyClick: (slug: String) -> Unit,
    onLoginClick: () -> Unit,
    onExploreClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LightStatusBarIcons()
    val side = Modifier.padding(horizontal = 20.dp)
    var pendingRemove by rememberSaveable { mutableStateOf<String?>(null) }
    state.items.firstOrNull { it.propertyId == pendingRemove }?.let { favorite ->
        ConfirmDialog(
            title = stringResource(R.string.favorites_remove_confirm_title),
            text = stringResource(R.string.favorites_remove_confirm_text, favorite.title),
            confirmLabel = stringResource(R.string.favorites_remove_confirm_action),
            dismissLabel = stringResource(R.string.favorites_remove_cancel),
            tone = ConfirmTone.NEUTRAL,
            eyebrow = stringResource(R.string.favorites_remove_confirm_eyebrow),
            onConfirm = {
                onAction(FavoritesAction.Remove(favorite.propertyId))
                pendingRemove = null
            },
            onDismiss = { pendingRemove = null },
        )
    }
    LazyColumn(
        modifier = modifier.fillMaxSize().background(ArizColors.SectionCanvas).statusBarScrim(),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        item(key = "header") { Header(count = if (state.session is SessionState.SignedIn && state.hasLoaded) state.visibleItems.size else null) }

        when {
            state.session is SessionState.SignedOut -> item(key = "signed-out") {
                CallToAction(
                    title = stringResource(R.string.favorites_signed_out_title),
                    description = stringResource(R.string.favorites_signed_out_description),
                    action = stringResource(R.string.favorites_signed_out_action),
                    onAction = onLoginClick,
                    modifier = side,
                )
            }

            state.session is SessionState.Restoring || (state.isLoading && !state.hasLoaded) -> item(key = "loading") {
                Box(Modifier.fillMaxWidth().height(160.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = ArizColors.Brand)
                }
            }

            state.error != null && !state.hasLoaded -> item(key = "error") {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(state.error.message, style = MaterialTheme.typography.bodyMedium, color = ArizColors.Muted, textAlign = TextAlign.Center)
                    ArizPrimaryButton(text = stringResource(R.string.favorites_retry), onClick = { onAction(FavoritesAction.Retry) })
                }
            }

            else -> {
                val visible = state.visibleItems
                if (visible.isEmpty()) {
                    item(key = "empty") {
                        CallToAction(
                            title = stringResource(R.string.favorites_empty_title),
                            description = stringResource(R.string.favorites_empty_description),
                            action = stringResource(R.string.favorites_empty_action),
                            onAction = onExploreClick,
                            modifier = side,
                            icon = Icons.Outlined.Search,
                        )
                    }
                } else {
                    items(visible, key = { it.id }) { favorite ->
                        FavoriteItem(
                            favorite = favorite,
                            onClick = { onPropertyClick(favorite.slug) },
                            onRemove = { pendingRemove = favorite.propertyId },
                            modifier = side,
                        )
                    }
                }
            }
        }
    }
}

/** Cabecera de la pestaña (la misma de Buscar y Cuenta); el contador de favoritos va a la derecha del título. */
@Composable
private fun Header(count: Int?) {
    TabHeader(
        eyebrow = stringResource(R.string.favorites_header_eyebrow),
        title = stringResource(R.string.favorites_header_title),
        subtitle = stringResource(R.string.favorites_header_description),
        trailing = count?.let {
            {
                Row(
                    modifier = Modifier.background(Color.White.copy(alpha = 0.12f), RoundedCornerShape(50)).padding(horizontal = 12.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(Icons.Filled.Favorite, contentDescription = null, modifier = Modifier.size(14.dp), tint = FavoriteSoft)
                    Text(
                        text = if (it == 1) stringResource(R.string.favorites_count_one) else stringResource(R.string.favorites_count_many, it),
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                    )
                }
            }
        },
    )
}

/** Tarjeta de una propiedad guardada: icono, título, fecha de guardado y corazón para quitarla. */
@Composable
private fun FavoriteItem(favorite: Favorite, onClick: () -> Unit, onRemove: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, top = 14.dp, bottom = 14.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(
                modifier = Modifier.size(48.dp).background(ArizColors.BrandSoft, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Outlined.Home, contentDescription = null, modifier = Modifier.size(24.dp), tint = ArizColors.Brand)
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text = favorite.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = ArizColors.Ink,
                    maxLines = 2,
                )
                favorite.savedAtMillis?.let { millis ->
                    Text(
                        text = stringResource(
                            R.string.favorites_saved_on,
                            SimpleDateFormat("dd MMM yyyy", Locale("es", "PE")).format(millis).replace(".", ""),
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = ArizColors.Muted,
                    )
                }
            }
            FavoriteHeart(
                isFavorite = true,
                onClick = onRemove,
                contentDescription = stringResource(R.string.favorites_remove),
            )
        }
    }
}

/** Estado vacío (y la invitación a iniciar sesión): un icono, un mensaje corto y un botón. */
@Composable
private fun CallToAction(
    title: String,
    description: String,
    action: String,
    onAction: () -> Unit,
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector = Icons.Filled.Favorite,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(
                modifier = Modifier.size(88.dp).background(ArizColors.BrandSoft, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(38.dp), tint = ArizColors.Brand)
            }
            Text(
                text = title,
                modifier = Modifier.padding(top = 6.dp),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                color = ArizColors.Brand,
                textAlign = TextAlign.Center,
            )
            Text(text = description, style = MaterialTheme.typography.bodyMedium, color = ArizColors.Muted, textAlign = TextAlign.Center)
            ArizPrimaryButton(text = action, onClick = onAction, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
        }
    }
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun FavoritesEmptyPreview() {
    ArizTheme {
        FavoritesContent(
            state = FavoritesUiState(session = SessionState.SignedOut),
            onAction = {},
            onPropertyClick = {},
            onLoginClick = {},
            onExploreClick = {},
        )
    }
}
