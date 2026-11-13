package com.tecsup.app_ariz.feature.listings.ui.detail

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tecsup.app_ariz.core.designsystem.component.ArizPrimaryButton
import com.tecsup.app_ariz.core.designsystem.component.ArizSectionHeader
import com.tecsup.app_ariz.core.designsystem.theme.ArizColors
import com.tecsup.app_ariz.core.favorites.ui.FavoritesUi
import com.tecsup.app_ariz.core.favorites.ui.rememberFavorites
import com.tecsup.app_ariz.core.leads.domain.LeadTarget
import com.tecsup.app_ariz.core.leads.ui.LeadFormSheet
import com.tecsup.app_ariz.core.ui.LightStatusBarIcons
import com.tecsup.app_ariz.core.ui.component.FavoriteHeart
import kotlinx.coroutines.launch
import com.tecsup.app_ariz.core.ui.component.ContactBar
import com.tecsup.app_ariz.core.ui.component.PhotoGallery
import com.tecsup.app_ariz.core.ui.component.PropertyCard
import com.tecsup.app_ariz.feature.listings.R
import com.tecsup.app_ariz.feature.listings.domain.model.PropertyDetail
import com.tecsup.app_ariz.feature.listings.ui.component.AdvertiserCard
import com.tecsup.app_ariz.feature.listings.ui.component.PropertyDescription
import com.tecsup.app_ariz.feature.listings.ui.component.PropertyFeatures
import com.tecsup.app_ariz.feature.listings.ui.component.PropertyHeader
import com.tecsup.app_ariz.feature.listings.ui.component.PropertyLocationSection
import com.tecsup.app_ariz.feature.listings.ui.component.PropertyPrice
import com.tecsup.app_ariz.feature.listings.ui.component.PropertySpecStrip

private val WhatsAppGreen = Color(0xFF25D366)

@Composable
fun PropertyDetailScreen(
    onBackClick: () -> Unit,
    onPropertyClick: (slug: String) -> Unit,
    onAgentClick: (agentId: String) -> Unit,
    onLoginRequired: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PropertyDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LightStatusBarIcons()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                PropertyDetailEvent.LoginRequired -> onLoginRequired()
            }
        }
    }
    val favorites = rememberFavorites(
        onLoginRequired = onLoginRequired,
        onError = { message -> scope.launch { snackbarHostState.showSnackbar(message) } },
    )

    PropertyDetailContent(
        state = state,
        onAction = viewModel::onAction,
        onBackClick = onBackClick,
        onPropertyClick = onPropertyClick,
        onAgentClick = onAgentClick,
        favorites = favorites,
        snackbarHostState = snackbarHostState,
        modifier = modifier,
    )
}

/** Contenido sin estado: recibe el [PropertyDetailUiState] y callbacks, así se previsualiza y se prueba. */
@Composable
fun PropertyDetailContent(
    state: PropertyDetailUiState,
    onAction: (PropertyDetailAction) -> Unit,
    onBackClick: () -> Unit,
    onPropertyClick: (slug: String) -> Unit,
    onAgentClick: (agentId: String) -> Unit,
    favorites: FavoritesUi,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    val uriHandler = LocalUriHandler.current
    val detail = state.detail
    // El texto se resuelve aquí (contexto composable) y se usa dentro del callback de clic.
    val whatsAppMessage = stringResource(R.string.listings_whatsapp_message, detail?.title.orEmpty())
    var showLeadForm by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        containerColor = ArizColors.SectionCanvas,
        contentWindowInsets = WindowInsets(0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (detail != null) ContactBar(
                whatsapp = detail.advertiser.whatsapp,
                phone = detail.advertiser.phone,
                whatsappMessage = whatsAppMessage,
                whatsappLabel = stringResource(R.string.listings_contact_whatsapp),
                callLabel = stringResource(R.string.listings_contact_call),
                messageLabel = stringResource(R.string.listings_contact_message),
                onMessageClick = { showLeadForm = true },
            )
        },
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when {
                state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = ArizColors.Brand)
                }

                detail == null -> ErrorState(
                    message = state.error?.message.orEmpty(),
                    onRetry = { onAction(PropertyDetailAction.Retry) },
                )

                else -> DetailBody(
                    detail = detail,
                    state = state,
                    onOpenMapsClick = {
                        uriHandler.openUri(
                            "https://www.google.com/maps/search/?api=1&query=" +
                                "${detail.location.latitude},${detail.location.longitude}",
                        )
                    },
                    onPropertyClick = onPropertyClick,
                    onAgentClick = onAgentClick,
                    favorites = favorites,
                    onPriceAlertClick = { onAction(PropertyDetailAction.CreatePriceAlert) },
                )
            }
            BackButton(onClick = onBackClick)
        }
    }

    if (showLeadForm && detail != null) {
        LeadFormSheet(
            target = LeadTarget.Property(detail.id),
            defaultMessage = stringResource(R.string.listings_lead_default_message),
            onDismiss = { showLeadForm = false },
        )
    }
}

@Composable
private fun DetailBody(
    detail: PropertyDetail,
    state: PropertyDetailUiState,
    onOpenMapsClick: () -> Unit,
    onPropertyClick: (String) -> Unit,
    onAgentClick: (String) -> Unit,
    favorites: FavoritesUi,
    onPriceAlertClick: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        PhotoGallery(
            images = detail.images,
            emptyLabel = stringResource(R.string.listings_no_photos),
        ) {
            FavoriteHeart(
                isFavorite = favorites.isFavorite(detail.id),
                onClick = { favorites.onToggle(detail.id) },
                contentDescription = stringResource(
                    if (favorites.isFavorite(detail.id)) R.string.listings_favorite_remove else R.string.listings_favorite_add,
                ),
                modifier = Modifier.align(Alignment.TopEnd).statusBarsPadding().padding(top = 12.dp, end = 12.dp),
            )
        }
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            PropertyHeader(detail)
            PropertyPrice(detail)
            PriceAlertButton(status = state.priceAlert, onClick = onPriceAlertClick)
            PropertySpecStrip(detail)
            PropertyDescription(detail.description)
            PropertyFeatures(detail)
            PropertyLocationSection(detail, onOpenMapsClick = onOpenMapsClick)
            AdvertiserCard(detail, onClick = { onAgentClick(detail.advertiser.id) })
        }
        if (state.similar.isNotEmpty()) SimilarSection(state = state, onPropertyClick = onPropertyClick, favorites = favorites)
    }
}

@Composable
private fun SimilarSection(state: PropertyDetailUiState, onPropertyClick: (String) -> Unit, favorites: FavoritesUi) {
    val gutter = 12.dp
    val cardWidth = (LocalConfiguration.current.screenWidthDp.dp - gutter * 2 - 12.dp) / 2
    Column(modifier = Modifier.padding(bottom = 24.dp)) {
        ArizSectionHeader(
            title = stringResource(R.string.listings_similar),
            subtitle = stringResource(R.string.listings_similar_subtitle),
            modifier = Modifier.padding(horizontal = gutter),
        )
        LazyRow(
            modifier = Modifier.padding(top = 16.dp),
            contentPadding = PaddingValues(horizontal = gutter),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(state.similar, key = { it.id }) { property ->
                PropertyCard(
                    property = property,
                    onClick = { onPropertyClick(property.slug) },
                    modifier = Modifier.width(cardWidth).height(340.dp),
                    isFavorite = favorites.isFavorite(property.id),
                    onFavoriteClick = { favorites.onToggle(property.id) },
                )
            }
        }
    }
}

/** "Avísame si baja de precio" (`PriceAlertButton` del web): estados guardando, creado y error. */
@Composable
private fun PriceAlertButton(status: PriceAlertStatus, onClick: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Surface(
            onClick = onClick,
            enabled = status != PriceAlertStatus.CREATING && status != PriceAlertStatus.CREATED,
            modifier = Modifier.fillMaxWidth().height(44.dp),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, ArizColors.Edge),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = stringResource(
                        when (status) {
                            PriceAlertStatus.CREATING -> R.string.listings_price_alert_creating
                            PriceAlertStatus.CREATED -> R.string.listings_price_alert_created
                            else -> R.string.listings_price_alert
                        },
                    ),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = ArizColors.Brand,
                )
            }
        }
        if (status == PriceAlertStatus.FAILED) {
            Text(
                text = stringResource(R.string.listings_price_alert_failed),
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                color = ArizColors.Error,
            )
        }
    }
}

@Composable
private fun BackButton(onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .statusBarsPadding()
            .padding(start = 8.dp, top = 8.dp)
            .background(Color.Black.copy(alpha = 0.3f), CircleShape),
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
            contentDescription = stringResource(R.string.listings_back),
            tint = Color.White,
        )
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = ArizColors.Muted,
            textAlign = TextAlign.Center,
        )
        ArizPrimaryButton(text = stringResource(R.string.listings_retry), onClick = onRetry)
    }
}
