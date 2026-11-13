package com.tecsup.app_ariz.feature.home.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.tecsup.app_ariz.core.common.AppError
import com.tecsup.app_ariz.core.designsystem.component.ArizSectionHeader
import com.tecsup.app_ariz.core.designsystem.theme.ArizColors
import com.tecsup.app_ariz.core.favorites.ui.FavoritesUi
import com.tecsup.app_ariz.core.model.project.ProjectSummary
import com.tecsup.app_ariz.core.model.property.PropertySummary
import com.tecsup.app_ariz.core.ui.component.ProjectCard
import com.tecsup.app_ariz.core.ui.component.PropertyCard
import com.tecsup.app_ariz.feature.home.R
import com.tecsup.app_ariz.feature.home.domain.model.HomeFeed

private val CardHeight = 340.dp
private val ScreenGutter = 12.dp
private val CardSpacing = 12.dp

/** Dos tarjetas por pantalla, como `w-[calc((100vw-2.25rem)/2)]` del web. */
@Composable
private fun cardWidth(): Dp = (LocalConfiguration.current.screenWidthDp.dp - ScreenGutter * 2 - CardSpacing) / 2

/** Las tres filas del inicio con datos reales: Proyectos Ariz, Propiedades destacadas y Proyectos destacados. */
@Composable
internal fun CatalogSections(
    isLoading: Boolean,
    feed: HomeFeed?,
    error: AppError?,
    onRetry: () -> Unit,
    onPropertyClick: (PropertySummary) -> Unit,
    onProjectClick: (ProjectSummary) -> Unit,
    onAllPropertiesClick: () -> Unit,
    onAllProjectsClick: () -> Unit,
    favorites: FavoritesUi,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        when {
            isLoading -> Box(
                modifier = Modifier.fillMaxWidth().height(CardHeight),
                contentAlignment = Alignment.Center,
            ) { CircularProgressIndicator(color = ArizColors.Brand) }

            error != null -> LoadError(error = error, onRetry = onRetry)

            feed != null -> {
                CardSection(
                    title = stringResource(R.string.home_section_ariz_projects),
                    subtitle = stringResource(R.string.home_section_ariz_projects_subtitle),
                    actionText = stringResource(R.string.home_section_ariz_projects_action),
                    onActionClick = onAllProjectsClick,
                    emptyMessage = stringResource(R.string.home_section_ariz_projects_empty),
                    items = feed.arizProjects,
                    key = { it.id },
                ) { project ->
                    ProjectCard(
                        project = project,
                        onClick = { onProjectClick(project) },
                        modifier = Modifier.width(cardWidth()).height(CardHeight),
                    )
                }
                CardSection(
                    title = stringResource(R.string.home_section_properties),
                    subtitle = stringResource(R.string.home_section_properties_subtitle),
                    actionText = stringResource(R.string.home_section_properties_action),
                    onActionClick = onAllPropertiesClick,
                    emptyMessage = stringResource(R.string.home_section_properties_empty),
                    items = feed.featuredProperties,
                    key = { it.id },
                ) { property ->
                    PropertyCard(
                        property = property,
                        onClick = { onPropertyClick(property) },
                        modifier = Modifier.width(cardWidth()).height(CardHeight),
                        isFavorite = favorites.isFavorite(property.id),
                        onFavoriteClick = { favorites.onToggle(property.id) },
                    )
                }
                // Como en el web, esta fila desaparece si no hay proyectos destacados.
                if (feed.featuredProjects.isNotEmpty()) {
                    CardSection(
                        title = stringResource(R.string.home_section_featured_projects),
                        subtitle = stringResource(R.string.home_section_featured_projects_subtitle),
                        actionText = stringResource(R.string.home_section_ariz_projects_action),
                        onActionClick = onAllProjectsClick,
                        emptyMessage = null,
                        items = feed.featuredProjects,
                        key = { it.id },
                    ) { project ->
                        ProjectCard(
                            project = project,
                            onClick = { onProjectClick(project) },
                            modifier = Modifier.width(cardWidth()).height(CardHeight),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun <T> CardSection(
    title: String,
    subtitle: String,
    actionText: String,
    onActionClick: () -> Unit,
    emptyMessage: String?,
    items: List<T>,
    key: (T) -> Any,
    card: @Composable (T) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 16.dp)) {
        ArizSectionHeader(
            title = title,
            subtitle = subtitle,
            actionText = actionText,
            onActionClick = onActionClick,
            modifier = Modifier.padding(horizontal = ScreenGutter),
        )
        if (items.isEmpty() && emptyMessage != null) {
            Surface(
                modifier = Modifier.fillMaxWidth().padding(horizontal = ScreenGutter).padding(top = 16.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, ArizColors.Edge),
            ) {
                Text(
                    text = emptyMessage,
                    modifier = Modifier.padding(24.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = ArizColors.Muted,
                    textAlign = TextAlign.Center,
                )
            }
        } else {
            LazyRow(
                modifier = Modifier.padding(top = 16.dp),
                contentPadding = PaddingValues(horizontal = ScreenGutter),
                horizontalArrangement = Arrangement.spacedBy(CardSpacing),
            ) {
                items(items, key = key) { item -> card(item) }
            }
        }
    }
}

@Composable
private fun LoadError(error: AppError, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = error.message,
            style = MaterialTheme.typography.bodyMedium,
            color = ArizColors.Muted,
            textAlign = TextAlign.Center,
        )
        com.tecsup.app_ariz.core.designsystem.component.ArizPrimaryButton(
            text = stringResource(R.string.home_retry),
            onClick = onRetry,
        )
    }
}
