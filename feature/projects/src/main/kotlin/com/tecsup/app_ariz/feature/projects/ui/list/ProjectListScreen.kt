package com.tecsup.app_ariz.feature.projects.ui.list

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tecsup.app_ariz.core.designsystem.component.ArizDropdownField
import com.tecsup.app_ariz.core.designsystem.component.ArizPrimaryButton
import com.tecsup.app_ariz.core.designsystem.component.ArizTextField
import com.tecsup.app_ariz.core.designsystem.theme.ArizColors
import com.tecsup.app_ariz.core.designsystem.theme.ArizTheme
import com.tecsup.app_ariz.core.model.common.Currency
import com.tecsup.app_ariz.core.model.project.ProjectOriginFilter
import com.tecsup.app_ariz.core.model.project.ProjectSortOrder
import com.tecsup.app_ariz.core.model.project.ProjectStage
import com.tecsup.app_ariz.core.ui.LightStatusBarIcons
import com.tecsup.app_ariz.core.ui.format.formatPrice
import com.tecsup.app_ariz.feature.projects.R
import com.tecsup.app_ariz.feature.projects.ui.component.ProjectListItem

private const val LoadMoreThreshold = 3

@Composable
fun ProjectListScreen(
    onBackClick: () -> Unit,
    onProjectClick: (slug: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProjectListViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ProjectListContent(
        state = state,
        onAction = viewModel::onAction,
        onBackClick = onBackClick,
        onProjectClick = onProjectClick,
        modifier = modifier,
    )
}

/** Contenido sin estado: se previsualiza y se prueba pasándole un [ProjectListUiState]. */
@Composable
fun ProjectListContent(
    state: ProjectListUiState,
    onAction: (ProjectListAction) -> Unit,
    onBackClick: () -> Unit,
    onProjectClick: (slug: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LightStatusBarIcons()
    val listState = rememberLazyListState()
    val nearEnd by remember {
        derivedStateOf {
            val last = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            last >= listState.layoutInfo.totalItemsCount - 1 - LoadMoreThreshold
        }
    }
    LaunchedEffect(nearEnd, state.items.size) {
        if (nearEnd && state.items.isNotEmpty()) onAction(ProjectListAction.LoadMore)
    }

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize().background(ArizColors.SectionCanvas),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item(key = "hero") { Hero(onBackClick = onBackClick) }
        item(key = "stages") { StageChips(selected = state.filters.stage, onSelected = { onAction(ProjectListAction.StageChanged(it)) }) }
        item(key = "filters") {
            FiltersCard(state = state, onAction = onAction, modifier = Modifier.padding(horizontal = 16.dp))
        }
        item(key = "header") { ResultsHeader(state = state, onAction = onAction, modifier = Modifier.padding(horizontal = 16.dp)) }

        when {
            state.isLoading -> item(key = "loading") {
                Box(Modifier.fillMaxWidth().height(160.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = ArizColors.Brand)
                }
            }

            state.error != null && state.items.isEmpty() -> item(key = "error") {
                MessageWithRetry(message = state.error.message, onRetry = { onAction(ProjectListAction.Retry) })
            }

            state.items.isEmpty() -> item(key = "empty") {
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = ArizColors.SurfaceMuted,
                    border = BorderStroke(1.dp, ArizColors.Edge),
                ) {
                    Text(
                        text = stringResource(R.string.projects_empty),
                        modifier = Modifier.padding(32.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = ArizColors.Muted,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            else -> {
                items(state.items, key = { it.id }) { project ->
                    ProjectListItem(
                        project = project,
                        onClick = { onProjectClick(project.slug) },
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                }
                if (state.isLoadingMore) item(key = "more") {
                    Box(Modifier.fillMaxWidth().padding(8.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = ArizColors.Brand)
                    }
                }
                if (state.error != null) item(key = "more-error") {
                    MessageWithRetry(message = state.error.message, onRetry = { onAction(ProjectListAction.Retry) })
                }
            }
        }
        item(key = "nav-space") { Box(Modifier.navigationBarsPadding()) }
    }
}

/** Cabecera azul del directorio: etiqueta, titular con "en el Perú" en amarillo y subtítulo. */
@Composable
private fun Hero(onBackClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(ArizColors.Brand)
            .statusBarsPadding()
            .padding(start = 8.dp, end = 20.dp, bottom = 28.dp),
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier.padding(top = 8.dp).background(Color.White.copy(alpha = 0.12f), CircleShape),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = stringResource(R.string.projects_back),
                tint = Color.White,
            )
        }
        Column(modifier = Modifier.padding(start = 12.dp, top = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = stringResource(R.string.projects_hero_badge),
                modifier = Modifier
                    .background(Color.White.copy(alpha = 0.12f), RoundedCornerShape(50))
                    .padding(horizontal = 12.dp, vertical = 5.dp),
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 1.6.sp),
                color = ArizColors.Accent,
            )
            Text(
                text = buildAnnotatedString {
                    append(stringResource(R.string.projects_hero_title_1))
                    withStyle(SpanStyle(color = ArizColors.Accent)) { append(stringResource(R.string.projects_hero_title_2)) }
                },
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                color = Color.White,
            )
            Text(
                text = stringResource(R.string.projects_hero_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.85f),
            )
        }
    }
}

/** Pestañas de etapa: "Todos los proyectos", Preventa, En construcción y Entregado. */
@Composable
private fun StageChips(selected: ProjectStage?, onSelected: (ProjectStage?) -> Unit) {
    val options: List<ProjectStage?> = listOf(null) + ProjectStage.entries
    LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(options, key = { it?.name ?: "all" }) { stage ->
            val isSelected = stage == selected
            Text(
                text = stringResource(stage.chipLabelRes()),
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(if (isSelected) ArizColors.Brand else MaterialTheme.colorScheme.surface)
                    .border(1.dp, if (isSelected) ArizColors.Brand else ArizColors.Edge, RoundedCornerShape(50))
                    .clickable { onSelected(stage) }
                    .padding(horizontal = 16.dp, vertical = 9.dp),
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = if (isSelected) Color.White else ArizColors.Brand,
            )
        }
    }
}

@Composable
private fun FiltersCard(state: ProjectListUiState, onAction: (ProjectListAction) -> Unit, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, ArizColors.Edge),
        shadowElevation = 2.dp,
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            ArizTextField(
                value = state.districtText,
                onValueChange = { onAction(ProjectListAction.DistrictChanged(it)) },
                label = stringResource(R.string.projects_filter_district),
                placeholder = stringResource(R.string.projects_filter_district_placeholder),
                imeAction = ImeAction.Search,
                onImeAction = { onAction(ProjectListAction.DistrictSubmitted) },
            )
            ArizDropdownField(
                label = stringResource(R.string.projects_filter_origin),
                selected = state.filters.origin,
                options = listOf(null, ProjectOriginFilter.ARIZ, ProjectOriginFilter.OTHERS),
                optionLabel = { stringResource(it.labelRes()) },
                onSelected = { onAction(ProjectListAction.OriginChanged(it)) },
                modifier = Modifier.fillMaxWidth(),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ArizDropdownField(
                    label = stringResource(R.string.projects_filter_currency),
                    selected = state.filters.currency,
                    options = Currency.entries,
                    optionLabel = { stringResource(if (it == Currency.PEN) R.string.projects_currency_pen else R.string.projects_currency_usd) },
                    onSelected = { onAction(ProjectListAction.CurrencyChanged(it)) },
                    modifier = Modifier.weight(1f),
                )
                ArizDropdownField(
                    label = stringResource(R.string.projects_filter_budget),
                    selected = state.filters.priceMax,
                    options = BudgetOptions,
                    optionLabel = { budget ->
                        if (budget == null) {
                            stringResource(R.string.projects_budget_none)
                        } else {
                            stringResource(R.string.projects_budget_up_to, formatPrice(budget.toDouble(), state.filters.currency))
                        }
                    },
                    onSelected = { onAction(ProjectListAction.BudgetChanged(it)) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun ResultsHeader(state: ProjectListUiState, onAction: (ProjectListAction) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Column {
            Text(
                text = stringResource(R.string.projects_results_eyebrow),
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.8.sp),
                color = ArizColors.Accent,
            )
            Text(
                text = stringResource(R.string.projects_results_title),
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black),
                color = ArizColors.Brand,
            )
            if (!state.isLoading) {
                Text(
                    text = if (state.total == 1) {
                        stringResource(R.string.projects_results_count_one)
                    } else {
                        stringResource(R.string.projects_results_count, state.total)
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = ArizColors.Muted,
                )
            }
        }
        ArizDropdownField(
            label = stringResource(R.string.projects_sort_label),
            selected = state.filters.sort,
            options = ProjectSortOrder.entries,
            optionLabel = { stringResource(it.labelRes()) },
            onSelected = { onAction(ProjectListAction.SortChanged(it)) },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun MessageWithRetry(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(text = message, style = MaterialTheme.typography.bodyMedium, color = ArizColors.Muted, textAlign = TextAlign.Center)
        ArizPrimaryButton(text = stringResource(R.string.projects_retry), onClick = onRetry)
    }
}

private fun ProjectStage?.chipLabelRes(): Int = when (this) {
    null -> R.string.projects_stage_all
    ProjectStage.PRE_SALE -> R.string.projects_stage_pre_sale
    ProjectStage.UNDER_CONSTRUCTION -> R.string.projects_stage_under_construction
    ProjectStage.DELIVERED -> R.string.projects_stage_delivered
}

private fun ProjectOriginFilter?.labelRes(): Int = when (this) {
    null -> R.string.projects_origin_all
    ProjectOriginFilter.ARIZ -> R.string.projects_origin_ariz
    ProjectOriginFilter.OTHERS -> R.string.projects_origin_others
}

private fun ProjectSortOrder.labelRes(): Int = when (this) {
    ProjectSortOrder.RECENT -> R.string.projects_sort_recent
    ProjectSortOrder.FEATURED -> R.string.projects_sort_featured
    ProjectSortOrder.PRICE_ASC -> R.string.projects_sort_price_asc
    ProjectSortOrder.PRICE_DESC -> R.string.projects_sort_price_desc
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun ProjectListContentPreview() {
    ArizTheme {
        ProjectListContent(
            state = ProjectListUiState(isLoading = false),
            onAction = {},
            onBackClick = {},
            onProjectClick = {},
        )
    }
}
