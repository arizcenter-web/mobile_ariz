package com.tecsup.app_ariz.feature.search.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tecsup.app_ariz.core.designsystem.component.ArizDropdownField
import com.tecsup.app_ariz.core.designsystem.component.ArizPrimaryButton
import com.tecsup.app_ariz.core.designsystem.component.ArizSecondaryButton
import com.tecsup.app_ariz.core.designsystem.component.ArizTextField
import com.tecsup.app_ariz.core.designsystem.theme.ArizColors
import com.tecsup.app_ariz.core.designsystem.theme.ArizTheme
import com.tecsup.app_ariz.core.favorites.ui.FavoritesUi
import com.tecsup.app_ariz.core.favorites.ui.rememberFavorites
import com.tecsup.app_ariz.core.model.property.ListingOperation
import com.tecsup.app_ariz.core.model.property.PropertySortOrder
import com.tecsup.app_ariz.core.model.property.PropertyTypeFilter
import com.tecsup.app_ariz.core.ui.component.PropertyListItem
import com.tecsup.app_ariz.feature.search.R

private const val LoadMoreThreshold = 3

@Composable
fun SearchScreen(
    onPropertyClick: (slug: String) -> Unit,
    onLoginRequired: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SearchViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val favorites = rememberFavorites(
        onLoginRequired = onLoginRequired,
        onError = { message -> scope.launch { snackbarHostState.showSnackbar(message) } },
    )

    Scaffold(
        modifier = modifier,
        containerColor = ArizColors.SectionCanvas,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets(0),
    ) { padding ->
        SearchContent(
            state = state,
            onAction = viewModel::onAction,
            onPropertyClick = onPropertyClick,
            favorites = favorites,
            modifier = Modifier.padding(padding),
        )
    }
}

/** Contenido sin estado: se previsualiza y se prueba pasándole un [SearchUiState]. */
@Composable
fun SearchContent(
    state: SearchUiState,
    onAction: (SearchAction) -> Unit,
    onPropertyClick: (slug: String) -> Unit,
    favorites: FavoritesUi,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()

    // Scroll infinito: al acercarse al final se pide la siguiente página.
    val nearEnd by remember {
        androidx.compose.runtime.derivedStateOf {
            val last = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            last >= listState.layoutInfo.totalItemsCount - 1 - LoadMoreThreshold
        }
    }
    LaunchedEffect(nearEnd, state.items.size) {
        if (nearEnd && state.items.isNotEmpty()) onAction(SearchAction.LoadMore)
    }

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item(key = "filters") { FiltersCard(state = state, onAction = onAction) }
        item(key = "header") { ResultsHeader(state = state, onAction = onAction) }

        when {
            state.isLoading -> item(key = "loading") {
                Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = ArizColors.Brand)
                }
            }

            state.error != null && state.items.isEmpty() -> item(key = "error") {
                MessageWithRetry(message = state.error.message, onRetry = { onAction(SearchAction.Retry) })
            }

            state.items.isEmpty() -> item(key = "empty") { EmptyResults() }

            else -> {
                items(state.items, key = { it.id }) { property ->
                    PropertyListItem(
                        property = property,
                        onClick = { onPropertyClick(property.slug) },
                        // El contacto (WhatsApp / llamada) vive en el detalle de la propiedad.
                        onContactClick = { onPropertyClick(property.slug) },
                        isFavorite = favorites.isFavorite(property.id),
                        onFavoriteClick = { favorites.onToggle(property.id) },
                    )
                }
                if (state.isLoadingMore) item(key = "more") {
                    Box(Modifier.fillMaxWidth().padding(8.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = ArizColors.Brand)
                    }
                }
                if (state.error != null) item(key = "more-error") {
                    MessageWithRetry(message = state.error.message, onRetry = { onAction(SearchAction.Retry) })
                }
            }
        }
    }
}

/** `PropertyFiltersBar` del web: distrito, operación, tipo, dormitorios y "Más filtros". */
@Composable
private fun FiltersCard(state: SearchUiState, onAction: (SearchAction) -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, ArizColors.Edge),
        shadowElevation = 2.dp,
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            ArizTextField(
                value = state.districtText,
                onValueChange = { onAction(SearchAction.DistrictChanged(it)) },
                label = stringResource(R.string.search_district_label),
                placeholder = stringResource(R.string.search_district_placeholder),
                imeAction = ImeAction.Search,
                onImeAction = { onAction(SearchAction.DistrictSubmitted) },
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ArizDropdownField(
                    label = stringResource(R.string.search_operation_label),
                    selected = state.filters.operation,
                    options = listOf(ListingOperation.SALE, ListingOperation.RENT),
                    optionLabel = { operationLabel(it) },
                    onSelected = { onAction(SearchAction.OperationChanged(it)) },
                    modifier = Modifier.weight(1f),
                )
                ArizDropdownField(
                    label = stringResource(R.string.search_rooms_label),
                    selected = state.filters.roomsMin,
                    options = listOf(null, 1, 2, 3, 4),
                    optionLabel = { roomsLabel(it) },
                    onSelected = { onAction(SearchAction.RoomsChanged(it)) },
                    modifier = Modifier.weight(1f),
                )
            }
            ArizDropdownField(
                label = stringResource(R.string.search_type_label),
                selected = state.filters.type,
                options = listOf(null) + PropertyTypeFilter.entries,
                optionLabel = { typeLabel(it) },
                onSelected = { onAction(SearchAction.TypeChanged(it)) },
                modifier = Modifier.fillMaxWidth(),
            )

            if (state.showMoreFilters) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ArizTextField(
                            value = state.priceMinText,
                            onValueChange = { onAction(SearchAction.PriceMinChanged(it)) },
                            label = stringResource(R.string.search_price_min),
                            modifier = Modifier.weight(1f),
                            keyboardType = KeyboardType.Number,
                        )
                        ArizTextField(
                            value = state.priceMaxText,
                            onValueChange = { onAction(SearchAction.PriceMaxChanged(it)) },
                            label = stringResource(R.string.search_price_max),
                            modifier = Modifier.weight(1f),
                            keyboardType = KeyboardType.Number,
                        )
                    }
                    ArizTextField(
                        value = state.areaMinText,
                        onValueChange = { onAction(SearchAction.AreaMinChanged(it)) },
                        label = stringResource(R.string.search_area_min),
                        modifier = Modifier.fillMaxWidth(),
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done,
                        onImeAction = { onAction(SearchAction.ApplyMoreFilters) },
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ArizSecondaryButton(
                    onClick = { onAction(SearchAction.ToggleMoreFilters) },
                    modifier = Modifier.weight(1f),
                ) {
                    Text(
                        text = stringResource(
                            if (state.showMoreFilters) R.string.search_fewer_filters else R.string.search_more_filters,
                        ),
                        fontWeight = FontWeight.Bold,
                        color = ArizColors.Brand,
                    )
                }
                ArizPrimaryButton(
                    text = stringResource(R.string.search_apply),
                    onClick = { onAction(SearchAction.ApplyMoreFilters) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun ResultsHeader(state: SearchUiState, onAction: (SearchAction) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Column {
            if (!state.isLoading) {
                Text(
                    text = if (state.total == 1) {
                        stringResource(R.string.search_total_one)
                    } else {
                        stringResource(R.string.search_total, state.total)
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = ArizColors.Muted,
                )
            }
            Text(
                text = state.filters.type?.let { typeLabel(it) } ?: stringResource(R.string.search_heading),
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.SemiBold),
                color = ArizColors.Ink,
            )
        }
        ArizDropdownField(
            label = stringResource(R.string.search_sort_label),
            selected = state.filters.sort,
            options = PropertySortOrder.entries,
            optionLabel = { sortLabel(it) },
            onSelected = { onAction(SearchAction.SortChanged(it)) },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun EmptyResults() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = ArizColors.SurfaceMuted,
        border = BorderStroke(1.dp, ArizColors.Edge),
    ) {
        Text(
            text = stringResource(R.string.search_empty),
            modifier = Modifier.padding(32.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = ArizColors.Muted,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun MessageWithRetry(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = ArizColors.Muted,
            textAlign = TextAlign.Center,
        )
        ArizPrimaryButton(text = stringResource(R.string.search_retry), onClick = onRetry)
    }
}

@Composable
private fun operationLabel(operation: ListingOperation): String = stringResource(
    if (operation == ListingOperation.RENT) R.string.search_operation_rent else R.string.search_operation_buy,
)

@Composable
private fun roomsLabel(rooms: Int?): String =
    if (rooms == null) stringResource(R.string.search_rooms_any) else stringResource(R.string.search_rooms_min, rooms)

@Composable
private fun typeLabel(type: PropertyTypeFilter?): String = stringResource(
    when (type) {
        null -> R.string.search_type_all
        PropertyTypeFilter.HOUSE -> R.string.search_type_houses
        PropertyTypeFilter.APARTMENT -> R.string.search_type_apartments
        PropertyTypeFilter.LAND -> R.string.search_type_land
        PropertyTypeFilter.COMMERCIAL -> R.string.search_type_commercial
    },
)

@Composable
private fun sortLabel(sort: PropertySortOrder): String = stringResource(
    when (sort) {
        PropertySortOrder.RECENT -> R.string.search_sort_recent
        PropertySortOrder.PRICE_ASC -> R.string.search_sort_price_asc
        PropertySortOrder.PRICE_DESC -> R.string.search_sort_price_desc
    },
)

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun SearchContentPreview() {
    ArizTheme {
        SearchContent(
            state = SearchUiState(isLoading = false, showMoreFilters = true),
            onAction = {},
            onPropertyClick = {},
            favorites = FavoritesUi(),
        )
    }
}
