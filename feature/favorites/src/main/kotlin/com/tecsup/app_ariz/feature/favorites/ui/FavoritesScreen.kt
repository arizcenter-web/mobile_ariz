package com.tecsup.app_ariz.feature.favorites.ui

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.AutoAwesome
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
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tecsup.app_ariz.core.designsystem.component.ArizPrimaryButton
import com.tecsup.app_ariz.core.designsystem.theme.ArizColors
import com.tecsup.app_ariz.core.designsystem.theme.ArizTheme
import com.tecsup.app_ariz.core.favorites.domain.Favorite
import com.tecsup.app_ariz.core.session.SessionState
import com.tecsup.app_ariz.core.ui.component.FavoriteHeart
import com.tecsup.app_ariz.feature.favorites.R
import java.text.SimpleDateFormat
import java.util.Locale

private val FavoriteRed = Color(0xFFEF4444)

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
    LazyColumn(
        modifier = modifier.fillMaxSize().background(ArizColors.SectionCanvas).statusBarsPadding(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item(key = "header") { Header() }

        when {
            state.session is SessionState.SignedOut -> item(key = "signed-out") {
                CallToAction(
                    eyebrow = null,
                    title = stringResource(R.string.favorites_signed_out_title),
                    description = stringResource(R.string.favorites_signed_out_description),
                    action = stringResource(R.string.favorites_signed_out_action),
                    onAction = onLoginClick,
                )
            }

            state.session is SessionState.Restoring || (state.isLoading && !state.hasLoaded) -> item(key = "loading") {
                Box(Modifier.fillMaxWidth().height(160.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = ArizColors.Brand)
                }
            }

            state.error != null && !state.hasLoaded -> item(key = "error") {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(state.error.message, style = MaterialTheme.typography.bodyMedium, color = ArizColors.Muted, textAlign = TextAlign.Center)
                    ArizPrimaryButton(text = stringResource(R.string.favorites_retry), onClick = { onAction(FavoritesAction.Retry) })
                }
            }

            else -> {
                val visible = state.visibleItems
                item(key = "list-header") { ListHeader(count = visible.size) }
                if (visible.isEmpty()) {
                    item(key = "empty") {
                        CallToAction(
                            eyebrow = stringResource(R.string.favorites_empty_eyebrow),
                            title = stringResource(R.string.favorites_empty_title),
                            description = stringResource(R.string.favorites_empty_description),
                            action = stringResource(R.string.favorites_empty_action),
                            onAction = onExploreClick,
                            icon = Icons.Outlined.Search,
                        )
                    }
                } else {
                    items(visible, key = { it.id }) { favorite ->
                        FavoriteItem(
                            favorite = favorite,
                            onClick = { onPropertyClick(favorite.slug) },
                            onRemove = { onAction(FavoritesAction.Remove(favorite.propertyId)) },
                        )
                    }
                }
            }
        }
    }
}

/** Cabecera de la página del web: corazón rojo, "Tu selección personal" y "Mis favoritos". */
@Composable
private fun Header() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, ArizColors.Edge),
        shadowElevation = 2.dp,
    ) {
        Column {
            Box(Modifier.fillMaxWidth().height(6.dp).background(ArizColors.Brand))
            Row(modifier = Modifier.padding(20.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Box(
                    modifier = Modifier.size(48.dp).background(FavoriteRed.copy(alpha = 0.1f), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Filled.Favorite, contentDescription = null, modifier = Modifier.size(24.dp), tint = FavoriteRed)
                }
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Outlined.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp), tint = ArizColors.Accent)
                        Text(
                            text = stringResource(R.string.favorites_header_eyebrow),
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 1.6.sp),
                            color = ArizColors.Accent,
                        )
                    }
                    Text(
                        text = stringResource(R.string.favorites_header_title),
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                        color = ArizColors.Brand,
                    )
                    Text(
                        text = stringResource(R.string.favorites_header_description),
                        style = MaterialTheme.typography.bodyMedium,
                        color = ArizColors.Muted,
                    )
                }
            }
        }
    }
}

@Composable
private fun ListHeader(count: Int) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Column {
            Text(
                text = stringResource(R.string.favorites_list_eyebrow),
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 1.8.sp),
                color = ArizColors.Accent,
            )
            Text(
                text = stringResource(R.string.favorites_list_title),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                color = ArizColors.Brand,
            )
        }
        Row(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(50))
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(Icons.Filled.Favorite, contentDescription = null, modifier = Modifier.size(16.dp), tint = FavoriteRed)
            Text(
                text = if (count == 1) stringResource(R.string.favorites_count_one) else stringResource(R.string.favorites_count_many, count),
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = ArizColors.Brand,
            )
        }
    }
}

/** Tarjeta de una propiedad guardada: icono, título, fecha de guardado y corazón para quitarla. */
@Composable
private fun FavoriteItem(favorite: Favorite, onClick: () -> Unit, onRemove: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, ArizColors.Edge),
        shadowElevation = 2.dp,
    ) {
        Column {
            Box(Modifier.fillMaxWidth().height(4.dp).background(ArizColors.Accent))
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                    Box(
                        modifier = Modifier.size(44.dp).background(ArizColors.BrandSoft, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Outlined.Home, contentDescription = null, modifier = Modifier.size(22.dp), tint = ArizColors.Brand)
                    }
                    FavoriteHeart(
                        isFavorite = true,
                        onClick = onRemove,
                        contentDescription = stringResource(R.string.favorites_remove),
                    )
                }
                Text(
                    text = favorite.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = ArizColors.Brand,
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
        }
    }
}

/** Estado vacío del web (y la invitación a iniciar sesión): ilustración, mensaje y un botón. */
@Composable
private fun CallToAction(
    eyebrow: String?,
    title: String,
    description: String,
    action: String,
    onAction: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, ArizColors.Edge),
        shadowElevation = 2.dp,
    ) {
        Column {
            Box(Modifier.fillMaxWidth().height(6.dp).background(ArizColors.Accent))
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Box(
                    modifier = Modifier.size(80.dp).background(FavoriteRed.copy(alpha = 0.1f), RoundedCornerShape(24.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Filled.Favorite, contentDescription = null, modifier = Modifier.size(36.dp), tint = FavoriteRed)
                }
                eyebrow?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 1.6.sp),
                        color = ArizColors.Accent,
                        textAlign = TextAlign.Center,
                    )
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black),
                    color = ArizColors.Brand,
                    textAlign = TextAlign.Center,
                )
                Text(text = description, style = MaterialTheme.typography.bodyMedium, color = ArizColors.Muted, textAlign = TextAlign.Center)
                ArizPrimaryButton(text = action, onClick = onAction, modifier = Modifier.fillMaxWidth())
            }
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
