package com.tecsup.app_ariz.feature.agents.ui.profile

import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tecsup.app_ariz.core.designsystem.component.ArizPrimaryButton
import com.tecsup.app_ariz.core.designsystem.theme.ArizColors
import com.tecsup.app_ariz.core.favorites.ui.FavoritesUi
import com.tecsup.app_ariz.core.favorites.ui.rememberFavorites
import com.tecsup.app_ariz.core.ui.LightStatusBarIcons
import com.tecsup.app_ariz.core.ui.component.ContactBar
import com.tecsup.app_ariz.core.ui.component.PropertyListItem
import com.tecsup.app_ariz.feature.agents.R
import com.tecsup.app_ariz.feature.agents.domain.model.AgentProfile
import kotlinx.coroutines.launch

private val WhatsAppGreen = Color(0xFF25D366)

@Composable
fun AgentProfileScreen(
    onBackClick: () -> Unit,
    onPropertyClick: (slug: String) -> Unit,
    onLoginRequired: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AgentProfileViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val favorites = rememberFavorites(
        onLoginRequired = onLoginRequired,
        onError = { message -> scope.launch { snackbarHostState.showSnackbar(message) } },
    )
    LightStatusBarIcons()

    AgentProfileContent(
        state = state,
        onAction = viewModel::onAction,
        onBackClick = onBackClick,
        onPropertyClick = onPropertyClick,
        favorites = favorites,
        snackbarHostState = snackbarHostState,
        modifier = modifier,
    )
}

/** Contenido sin estado: recibe el [AgentProfileUiState] y callbacks, así se previsualiza y se prueba. */
@Composable
fun AgentProfileContent(
    state: AgentProfileUiState,
    onAction: (AgentProfileAction) -> Unit,
    onBackClick: () -> Unit,
    onPropertyClick: (slug: String) -> Unit,
    favorites: FavoritesUi,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    val profile = state.profile
    val whatsAppMessage = stringResource(R.string.agents_whatsapp_message, profile?.firstName.orEmpty())

    Scaffold(
        modifier = modifier,
        containerColor = ArizColors.SectionCanvas,
        contentWindowInsets = WindowInsets(0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (profile != null) ContactBar(
                whatsapp = profile.whatsappDigits,
                phone = profile.phone,
                whatsappMessage = whatsAppMessage,
                whatsappLabel = stringResource(R.string.agents_contact_whatsapp),
                callLabel = stringResource(R.string.agents_contact_call),
            )
        },
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when {
                state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = ArizColors.Brand)
                }

                profile == null -> Column(
                    modifier = Modifier.fillMaxSize().padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
                ) {
                    Text(
                        text = state.error?.message.orEmpty(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = ArizColors.Muted,
                        textAlign = TextAlign.Center,
                    )
                    ArizPrimaryButton(text = stringResource(R.string.agents_retry), onClick = { onAction(AgentProfileAction.Retry) })
                }

                else -> ProfileBody(profile = profile, onPropertyClick = onPropertyClick, favorites = favorites)
            }
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(start = 8.dp, top = 8.dp)
                    .background(Color.White.copy(alpha = 0.15f), CircleShape),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = stringResource(R.string.agents_back),
                    tint = Color.White,
                )
            }
        }
    }
}

@Composable
private fun ProfileBody(profile: AgentProfile, onPropertyClick: (String) -> Unit, favorites: FavoritesUi) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item(key = "hero") { Hero(profile) }
        item(key = "contact") { ContactSection(profile, modifier = Modifier.padding(horizontal = 16.dp)) }
        item(key = "portfolio-header") { PortfolioHeader(profile.properties.size, modifier = Modifier.padding(horizontal = 16.dp)) }

        if (profile.properties.isEmpty()) {
            item(key = "empty") {
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = ArizColors.SurfaceMuted,
                    border = BorderStroke(1.dp, ArizColors.Edge),
                ) {
                    Text(
                        text = stringResource(R.string.agents_portfolio_empty),
                        modifier = Modifier.padding(32.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = ArizColors.Muted,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        } else {
            items(profile.properties, key = { it.id }) { property ->
                PropertyListItem(
                    property = property,
                    onClick = { onPropertyClick(property.slug) },
                    onContactClick = { onPropertyClick(property.slug) },
                    modifier = Modifier.padding(horizontal = 16.dp),
                    isFavorite = favorites.isFavorite(property.id),
                    onFavoriteClick = { favorites.onToggle(property.id) },
                )
            }
        }
        item(key = "nav-space") { Box(Modifier.navigationBarsPadding()) }
    }
}

/** Cabecera azul: insignias, nombre, rol y zona, y las cifras (propiedades, zona y WhatsApp). */
@Composable
private fun Hero(profile: AgentProfile) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(ArizColors.Brand)
            .statusBarsPadding()
            .padding(start = 20.dp, end = 20.dp, top = 64.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Box(
            modifier = Modifier.size(72.dp).background(ArizColors.Accent, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = profile.initials,
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                color = ArizColors.OnAccent,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Badge(stringResource(R.string.agents_badge_advertiser))
            if (profile.isVerified) {
                Badge(stringResource(R.string.agents_badge_verified), withCheck = true)
            }
        }
        Text(
            text = profile.name,
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
            color = Color.White,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (profile.locationLine.isNotEmpty()) {
                Icon(Icons.Outlined.Place, contentDescription = null, modifier = Modifier.padding(top = 2.dp).size(16.dp), tint = ArizColors.Accent)
            }
            Text(
                text = if (profile.locationLine.isNotEmpty()) {
                    stringResource(R.string.agents_role_with_zone, profile.locationLine)
                } else {
                    stringResource(R.string.agents_role)
                },
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = Color.White.copy(alpha = 0.8f),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            Stat(
                value = profile.properties.size.toString(),
                label = stringResource(
                    if (profile.properties.size == 1) R.string.agents_stat_property_one else R.string.agents_stat_property_many,
                ),
            )
            if (profile.serviceZone.isNotBlank()) {
                Stat(value = profile.serviceZone, label = stringResource(R.string.agents_stat_zone), modifier = Modifier.weight(1f, fill = false))
            }
        }
        profile.whatsappDisplay?.let { Stat(value = it, label = stringResource(R.string.agents_stat_whatsapp)) }
    }
}

@Composable
private fun Badge(text: String, withCheck: Boolean = false) {
    Row(
        modifier = Modifier
            .background(ArizColors.Accent.copy(alpha = 0.15f), RoundedCornerShape(50))
            .padding(horizontal = 12.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        if (withCheck) Icon(Icons.Outlined.Check, contentDescription = null, modifier = Modifier.size(14.dp), tint = ArizColors.Accent)
        Text(text, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold), color = ArizColors.Accent)
    }
}

@Composable
private fun Stat(value: String, label: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(value, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black), color = Color.White)
        Text(label, style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp), color = Color.White.copy(alpha = 0.7f))
    }
}

/** "Habla con {nombre}": WhatsApp, correo y sitio web (la llamada está en la barra inferior). */
@Composable
private fun ContactSection(profile: AgentProfile, modifier: Modifier = Modifier) {
    val uriHandler = LocalUriHandler.current
    val whatsAppMessage = stringResource(R.string.agents_whatsapp_message, profile.firstName)
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, ArizColors.Edge),
        shadowElevation = 2.dp,
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = stringResource(R.string.agents_contact_eyebrow),
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 1.8.sp),
                color = ArizColors.Accent,
            )
            Text(
                text = stringResource(R.string.agents_contact_title, profile.firstName),
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black),
                color = ArizColors.Brand,
            )
            Text(
                text = stringResource(R.string.agents_contact_description),
                style = MaterialTheme.typography.bodyMedium,
                color = ArizColors.Muted,
            )
            profile.whatsappDigits?.let { digits ->
                Surface(
                    onClick = { uriHandler.openUri("https://wa.me/$digits?text=${Uri.encode(whatsAppMessage)}") },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = MaterialTheme.shapes.medium,
                    color = WhatsAppGreen,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = stringResource(R.string.agents_contact_whatsapp),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium),
                            color = Color.White,
                        )
                    }
                }
            }
            profile.email?.let { email ->
                LinkRow(text = stringResource(R.string.agents_contact_email), onClick = { uriHandler.openUri("mailto:$email") })
            }
            profile.websiteUrl?.let { url ->
                LinkRow(text = url.removePrefix("https://").removePrefix("http://"), onClick = { uriHandler.openUri(url) })
            }
        }
    }
}

@Composable
private fun LinkRow(text: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = ArizColors.SurfaceMuted,
        border = BorderStroke(1.dp, ArizColors.Edge),
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = ArizColors.Brand,
            maxLines = 1,
        )
    }
}

@Composable
private fun PortfolioHeader(count: Int, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            text = stringResource(R.string.agents_portfolio_eyebrow),
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 1.8.sp),
            color = ArizColors.Accent,
        )
        Text(
            text = stringResource(R.string.agents_portfolio_title),
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black),
            color = ArizColors.Brand,
        )
        Text(
            text = if (count == 1) stringResource(R.string.agents_portfolio_count_one) else stringResource(R.string.agents_portfolio_count_many, count),
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = ArizColors.Muted,
        )
    }
}
